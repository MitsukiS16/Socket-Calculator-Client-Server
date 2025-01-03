import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.*;

/**
 * The Peer class represents a node in a distributed peer-to-peer network.
 * It is responsible for managing peer connections, message exchange, and maintaining
 * a Lamport logical clock to ensure event ordering.
 */
public class Peer implements Runnable {

    // Logger to log peer activities
    private static final Logger logger = Logger.getLogger(Peer.class.getName());

    // Configuration constants
    private static final int MESSAGE_QUEUE_CAPACITY = 100; // Capacity for the message queue
    private static final double POISSON_RATE = 1.0; // Rate parameter for Poisson process

    // Host and port for this peer
    private final String host;
    private final int port;

    // List of connected peers
    private final List<String> peerList;

    // Lamport clock for logical timekeeping
    private final LamportClock lamportClock;

    // Queue to manage incoming messages in order of timestamps
    private final PriorityBlockingQueue<Message> messageQueue;

    // Server socket to listen for incoming connections
    private ServerSocket serverSocket;

    // Flag to indicate if the peer is running
    private volatile boolean running;

    /**
     * Constructor to initialize a Peer instance.
     *
     * @param host The host address for this peer.
     * @param port The port number for this peer.
     */
    public Peer(String host, int port) {
        this.host = host;
        this.port = port;
        this.peerList = Collections.synchronizedList(new ArrayList<>());
        this.lamportClock = new LamportClock(port);
        this.messageQueue = new PriorityBlockingQueue<>(MESSAGE_QUEUE_CAPACITY, Comparator.comparingLong(Message::getTimestamp));
        this.running = true;
        setupLogger();
    }

    /**
     * Sets up the logger to write logs to a file.
     */
    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("peer_" + port + ".log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

    /**
     * Adds a peer to the list of connected peers.
     *
     * @param ip   The IP address of the peer.
     * @param port The port number of the peer.
     */
    public void addPeer(String ip, int port) {
        synchronized (peerList) {
            peerList.add(ip + ":" + port);
            logger.info("Added peer: " + ip + ":" + port);
        }
    }

    /**
     * The main execution logic for the Peer.
     * Starts a server socket to listen for incoming messages and sends messages to peers.
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Peer running on " + host + ":" + port);

            // Start a thread to listen for messages
            new Thread(this::handleIncomingConnections).start();

            // Begin sending messages
            initiateMessageSending();

        } catch (IOException e) {
            logger.severe("Error starting Peer: " + e.getMessage());
        } finally {
            close();
        }
    }

    /**
     * Listens for incoming messages from other peers.
     */
    private void handleIncomingConnections() {
        while (running) {
            try (Socket clientSocket = serverSocket.accept();
                 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                // Deserialize the received message
                Message receivedMessage = (Message) in.readObject();
                logger.info("Received message: " + receivedMessage);

                // Update the Lamport clock and queue the message
                lamportClock.increment();
                lamportClock.setTime(Math.max(lamportClock.getTime(), receivedMessage.getTimestamp()));
                messageQueue.offer(receivedMessage);

                // Process the messages in the queue
                processQueuedMessages();

            } catch (IOException | ClassNotFoundException e) {
                if (running) {
                    logger.warning("Error receiving message: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Sends messages to all connected peers at intervals determined by a Poisson process.
     */
    private void initiateMessageSending() {
        PoissonProcess poissonProcess = new PoissonProcess(POISSON_RATE, new Random());
        Random random = new Random();
        List<String> words = loadWordList();

        if (words.isEmpty()) {
            logger.severe("Word list is empty. Cannot send messages.");
            return;
        }

        while (running) {
            try {
                // Generate a random word
                String word = words.get(random.nextInt(words.size()));
                logger.fine("Generated word: " + word);

                // Increment the Lamport clock and create a message
                lamportClock.increment();
                Message message = new Message(word, lamportClock.getTime(), port);

                // Send the message to all peers
                synchronized (peerList) {
                    if (peerList.isEmpty()) {
                        logger.severe("No peers in the peer list. Cannot send messages.");
                        return;
                    }

                    for (String peer : peerList) {
                        String[] peerData = peer.split(":");
                        transmitMessageToPeer(peerData[0], Integer.parseInt(peerData[1]), message);
                    }
                }

                // Wait for the next message interval
                Thread.sleep((long) (poissonProcess.calculate(0) * 1000));

            } catch (IOException | InterruptedException e) {
                logger.warning("Error sending message: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a message to a specific peer.
     *
     * @param ip      The IP address of the peer.
     * @param port    The port number of the peer.
     * @param message The message to send.
     * @throws IOException If an error occurs during sending.
     */
    private void transmitMessageToPeer(String ip, int port, Message message) throws IOException {
        try (Socket socket = new Socket(ip, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(message);
            logger.info("Sent message to " + ip + ":" + port + " -> " + message);
        } catch (IOException e) {
            logger.warning("Failed to send message to " + ip + ":" + port + ": " + e.getMessage());
        }
    }

    /**
     * Processes messages in the queue if they can be processed in order.
     */
    private void processQueuedMessages() {
        logger.fine("Attempting to process messages...");
        while (!messageQueue.isEmpty() && isMessageProcessable(messageQueue.peek())) {
            Message message = messageQueue.poll();
            logger.info("Processed message: " + message.getContent());
            System.out.println(message.getContent()); // Print the processed word
        }
    }

    /**
     * Checks if a message can be processed based on Lamport clock ordering.
     *
     * @param message The message to check.
     * @return True if the message can be processed; false otherwise.
     */
    private boolean isMessageProcessable(Message message) {
        logger.fine("Checking if message can be processed: " + message);
        synchronized (messageQueue) {
            for (Message queuedMessage : messageQueue) {
                if (queuedMessage.getTimestamp() < message.getTimestamp()) {
                    logger.fine("Cannot process due to earlier message: " + queuedMessage);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Loads a predefined list of words for message generation.
     *
     * @return A list of words.
     */
    private List<String> loadWordList() {
        return Arrays.asList("apple", "banana", "cherry", "date", "elderberry", "fig", "grape", "honeydew");
    }

    /**
     * Stops the Peer by setting the running flag to false.
     */
    public void stop() {
        running = false;
    }

    /**
     * Closes the Peer by stopping it and releasing resources.
     */
    public void close() {
        stop();
        if (serverSocket != null) {
            try {
                serverSocket.close();
                logger.info("Server socket closed.");
            } catch (IOException e) {
                logger.severe("Error closing server socket: " + e.getMessage());
            }
        }
    }
}