

import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * Server class responsible for managing incoming connections and forwarding tokens.
 */
public class Server implements Runnable {
    private final String currentHost; // Current server's host address
    private final int currentPort; // Current server's port number
    private final ServerSocket serverSocket; // Server socket to accept connections
    private final Logger logger; // Logger for server events

    private final Queue<String> operationQueue = new ConcurrentLinkedQueue<>(); // Thread-safe queue for operations
    private final String nextHost; // Next server's host address
    private final int nextPort; // Next server's port number
    private volatile String receivedToken; // Token received from the previous server

    /**
     * Constructor for the Server class.
     *
     * @param currentHost Host address of the server.
     * @param currentPort Port number of the server.
     * @param logger Logger instance for logging events.
     * @param nextHost Host address of the next server in the chain.
     * @param nextPort Port number of the next server in the chain.
     * @throws IOException If an error occurs while creating the server socket.
     */
    public Server(String currentHost, int currentPort, Logger logger, String nextHost, int nextPort) throws IOException {
        this.currentHost = currentHost;
        this.currentPort = currentPort;
        this.logger = logger;
        this.serverSocket = new ServerSocket(currentPort, 1, InetAddress.getByName(currentHost));
        this.nextHost = nextHost;
        this.nextPort = nextPort;
        logger.info(String.format("Server initialized at %s:%d, next server is at %s:%d", currentHost, currentPort, nextHost, nextPort));
    }

    /**
     * Adds an operation to the operation queue.
     *
     * @param operation The operation to be added.
     */
    public void enqueueOperation(String operation) {
        operationQueue.add(operation);
        logger.info("Operation added to queue: " + operation);
    }

    /**
     * Main server loop to accept connections and process tokens.
     */
    @Override
    public void run() {
        logger.info("Server running at " + currentHost + ":" + currentPort);
        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                receivedToken = inputReader.readLine();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                logger.info("Received connection from " + clientAddress);

                if ("token".equals(receivedToken)) {
                    logger.info("Token received, starting operation processing.");
                    processOperations();
                    forwardToken();
                } else {
                    logger.warning("Invalid token received: " + receivedToken);
                }

            } catch (IOException e) {
                logger.severe("Error handling client connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes all operations in the queue and sends results to a calculator service.
     */
    private void processOperations() {
        while (!operationQueue.isEmpty()) {
            String operation = operationQueue.poll();
            if (operation != null) {
                try (Socket calcSocket = new Socket("localhost", 3000);
                     PrintWriter calcWriter = new PrintWriter(calcSocket.getOutputStream(), true);
                     BufferedReader calcReader = new BufferedReader(new InputStreamReader(calcSocket.getInputStream()))) {

                    // Log the operation being processed
                    logger.info("Processing operation: " + operation);

                    calcWriter.println(operation);
                    calcWriter.flush();

                    // Log the result of the operation
                    String result = calcReader.readLine();
                    logger.info("Processed operation: " + operation + ", Result: " + result);

                } catch (IOException e) {
                    logger.severe("Error communicating with calculator service for operation: " + operation + ", Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                logger.warning("Operation queue is empty, no operation to process.");
            }
        }
    }

    /**
     * Forwards the token to the next server in the chain.
     */
    private void forwardToken() {
        try (Socket nextServerSocket = new Socket(nextHost, nextPort);
             PrintWriter out = new PrintWriter(nextServerSocket.getOutputStream(), true)) {

            out.println("token");
            out.flush();
            logger.info("Token forwarded to " + nextHost + ":" + nextPort);

        } catch (IOException e) {
            logger.severe("Error forwarding token to next server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
