import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Peer class simulates a node in a peer-to-peer network.
 * 
 * Responsibilities:
 * 1. Connect to other peers and maintain active connections.
 * 2. Listen for incoming connections and merge peer maps.
 * 3. Periodically broadcast its peer map to a random connected peer (anti-entropy).
 * 4. Remove stale peers from the peer map to maintain a healthy network.
 */
public class Peer implements Runnable {

    private static final Logger logger = Logger.getLogger(Peer.class.getName());
    private static final double LAMBDA = 5.0;
    private static final int CONNECTION_TIMEOUT = 3 * 1000;

    private final String mainHost;
    private final int mainPort;
    private final Map<Map.Entry<String, Integer>, Integer> nextPeers;
    private final Map<Map.Entry<String, Integer>, Socket> connections;
    private final PeerMap peerMap;
    private final Random rng;
    private final PoissonProcess poisson;
    private ServerSocket serverSocket;
    private volatile boolean running = true; // Control flag for thread execution

    public Peer(String mainHost, int mainPort, Map<Map.Entry<String, Integer>, Integer> nextPeers) throws IOException {
        this.mainHost = mainHost;
        this.mainPort = mainPort;
        this.nextPeers = nextPeers;
        this.connections = new ConcurrentHashMap<>();
        this.peerMap = new PeerMap(mainHost + ":" + mainPort);
        this.rng = new Random(System.currentTimeMillis());
        this.poisson = new PoissonProcess(LAMBDA, rng);
        this.serverSocket = new ServerSocket(mainPort);
        setupLogger();
    }

    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("peer.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("Starting peer on " + mainHost + ":" + mainPort);
        logger.info("Starting peer on " + mainHost + ":" + mainPort);

        new Thread(this::listenForIncomingConnections).start();
        establishConnectionsWithKnownPeers();
        initiatePeriodicCleanup();
        initiatePeriodicPeerMapBroadcast();
    }

    private void listenForIncomingConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                if (!running) break;
                System.out.println("New connection from: " + clientSocket.getRemoteSocketAddress());
                logger.info("New connection from: " + clientSocket.getRemoteSocketAddress());
                handleDataFromConnectedPeer(clientSocket);
            } catch (IOException e) {
                if (running) {
                    System.out.println("Error accepting connection: " + e.getMessage());
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private void handleDataFromConnectedPeer(Socket clientSocket) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String line;
                while (running && (line = reader.readLine()) != null) {
                    PeerMap receivedMap = PeerMap.deserialize(line);
                    if (receivedMap == null) {
                        System.out.println("Failed to deserialize incoming data");
                        logger.warning("Failed to deserialize incoming data");
                        continue;
                    }
                    System.out.println("Merging peer map from: " + clientSocket.getRemoteSocketAddress());
                    logger.info("Merging peer map from: " + clientSocket.getRemoteSocketAddress());
                    peerMap.merge(receivedMap);

                    receivedMap.getPeerTimestamps().forEach((peer, timestamp) -> {
                        Map.Entry<String, Integer> peerEntry = Map.entry(peer.getKey(), peer.getValue());
                        if (!connections.containsKey(peerEntry)) {
                            retryConnectionToPeer(peerEntry);
                        }
                    });
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("Error handling incoming data: " + e.getMessage());
                    logger.warning("Error handling incoming data: " + e.getMessage());
                }
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                    logger.warning("Error closing connection: " + e.getMessage());
                }
            }
        }).start();
    }

    private void establishConnectionsWithKnownPeers() {
        for (Map.Entry<Map.Entry<String, Integer>, Integer> entry : nextPeers.entrySet()) {
            retryConnectionToPeer(entry.getKey());
        }
    }

    private void retryConnectionToPeer(Map.Entry<String, Integer> peerInfo) {
        String host = peerInfo.getKey();
        int port = peerInfo.getValue();

        System.out.println("Attempting to connect to peer: " + host + ":" + port);
        logger.info("Attempting to connect to peer: " + host + ":" + port);

        long endTime = System.currentTimeMillis() + CONNECTION_TIMEOUT;
        while (running && System.currentTimeMillis() < endTime) {
            try {
                Socket socket = new Socket(host, port);
                connections.put(peerInfo, socket);
                System.out.println("Connected to peer: " + host + ":" + port);
                logger.info("Connected to peer: " + host + ":" + port);
                return;
            } catch (IOException e) {
                // Retry until timeout
            }
        }
        if (running) {
            System.out.println("Failed to connect to peer within timeout: " + host + ":" + port);
            logger.warning("Failed to connect to peer within timeout: " + host + ":" + port);
        }
    }

    private void initiatePeriodicPeerMapBroadcast() {
        new Thread(() -> {
            while (running) {
                if (connections.isEmpty()) {
                    continue;
                }

                try {
                    List<Map.Entry<String, Integer>> peerKeys = new ArrayList<>(connections.keySet());
                    Map.Entry<String, Integer> randomPeer = peerKeys.get(rng.nextInt(peerKeys.size()));
                    Socket socket = connections.get(randomPeer);

                    byte[] serializedMap = peerMap.serialize().getBytes();
                    OutputStream out = socket.getOutputStream();
                    out.write(serializedMap);
                    out.flush();

                    System.out.println("Sent peer map to " + randomPeer.getKey() + ":" + randomPeer.getValue());
                    logger.info("Sent peer map to " + randomPeer.getKey() + ":" + randomPeer.getValue());

                    long waitTime = (long) (poisson.calculate(0) * 60 * 1000);
                    Thread.sleep(waitTime);
                } catch (IOException | InterruptedException e) {
                    if (running) {
                        System.out.println("Error during anti-entropy broadcast: " + e.getMessage());
                        logger.warning("Error during anti-entropy broadcast: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void initiatePeriodicCleanup() {
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(60000);
                    peerMap.removeStaleEntries(Duration.ofMinutes(5));
                    System.out.println("Stale peers removed from PeerMap.");
                    logger.info("Stale peers removed from PeerMap.");
                } catch (InterruptedException e) {
                    if (running) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }

    public void close() {
        System.out.println("Shutting down peer...");
        logger.info("Shutting down peer...");
        running = false; // Signal threads to stop
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing server socket: " + e.getMessage());
            logger.warning("Error closing server socket: " + e.getMessage());
        }
        for (Socket socket : connections.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing peer connection: " + e.getMessage());
                logger.warning("Error closing peer connection: " + e.getMessage());
            }
        }
    }
}
