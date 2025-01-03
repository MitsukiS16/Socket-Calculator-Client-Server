

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RequestGenerator simulates sending requests to a server at intervals determined by a Poisson process.
 */
public class RequestGenerator implements Runnable {

    private final String targetHost;  // Target server hostname or IP address
    private final int targetPort;  // Target server port number
    private final Logger logger;  // Logger for logging events and debugging
    private final PoissonProcess poissonProcess;  // Poisson process instance for interval calculation
    private final Server server;  // Server instance for handling operations
    private final AtomicBoolean isRunning;  // Controls the thread's execution state

    private static final int MAX_RETRIES = 3;  // Maximum retries for sending requests
    private static final int RETRY_INTERVAL_MS = 2000;  // Interval between retries (2 seconds)

    /**
     * Constructor for RequestGenerator.
     *
     * @param targetHost       Address of the server to send requests to.
     * @param logger           Logger instance for logging.
     * @param poissonProcess   PoissonProcess instance for generating inter-arrival times.
     * @param targetPort       Port number of the server.
     * @param server           Server instance for operation handling.
     */
    public RequestGenerator(String targetHost, Logger logger, PoissonProcess poissonProcess, int targetPort, Server server) {
        this.targetHost = targetHost;
        this.logger = logger;
        this.poissonProcess = poissonProcess;
        this.targetPort = targetPort;
        this.server = server;
        this.isRunning = new AtomicBoolean(true);  // Allow the thread to be controlled
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                // Calculate the time to wait before sending the next request, in milliseconds
                double interArrivalTimeMs = poissonProcess.calculate(0) * 1000;
                logger.info(String.format("Waiting %.2f milliseconds before sending the next request.", interArrivalTimeMs));

                // Pause execution for the calculated interval
                Thread.sleep((long) interArrivalTimeMs);

                // Generate and send a new request
                String request = createRandomRequest();
                logger.info(String.format("Generated request: %s", request));

                // Retry sending the request in case of failure
                if (sendRequest(request)) {
                    // Add operation to the server's queue
                    synchronized (server) {
                        server.enqueueOperation(request);
                        logger.info("Request added to server operations.");
                    }
                } else {
                    logger.warning("Request failed after maximum retries. Skipping operation.");
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Thread interrupted while waiting to send a request.", e);
                Thread.currentThread().interrupt();
                break;  // Exit the loop gracefully
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error occurred while generating requests.", e);
            }
        }
    }

    /**
     * Creates a random mathematical operation request.
     *
     * @return A string representing the request (e.g., "add:12.34:56.78").
     */
    private String createRandomRequest() {
        String[] operations = { "add", "sub", "mul", "div" };
        Random random = new Random();

        // Randomly select an operation
        String operation = operations[random.nextInt(operations.length)];

        // Generate two random operands for the operation
        double operand1 = random.nextDouble() * 100;
        double operand2 = random.nextDouble() * 100;

        // Format the request as "operation:operand1:operand2"
        return String.format("%s:%.2f:%.2f", operation, operand1, operand2).replace(',', '.');
    }

    /**
     * Sends the request to the specified server with retry logic.
     *
     * @param request The request string to send.
     * @return true if the request was successfully sent, false after retries have been exhausted.
     */
    private boolean sendRequest(String request) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            attempt++;
            try (Socket socket = new Socket(targetHost, targetPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println(request);
                out.flush();
                logger.info(String.format("Request sent to server: %s on attempt %d/%d", request, attempt, MAX_RETRIES));
                return true;  // Successfully sent the request
            } catch (IOException e) {
                logger.log(Level.WARNING, String.format("Failed to send request to server on attempt %d/%d. Retrying...", attempt, MAX_RETRIES), e);
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);  // Wait before retrying
                } catch (InterruptedException ie) {
                    logger.log(Level.SEVERE, "Retry interrupted", ie);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        logger.severe(String.format("Request failed after %d retries: %s", MAX_RETRIES, request));
        return false;  // Failed to send request after max retries
    }

    /**
     * Gracefully stop the request generation process.
     */
    public void stop() {
        isRunning.set(false);
        logger.info("Request generation stopped.");
    }
}
