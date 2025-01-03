

import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

/**
 * Peer class represents a network peer with logging and request handling capabilities.
 */
public class Peer {
    private final String currentHost; // Current host address
    private final Logger logger; // Logger for peer operations
    private final PoissonProcess poissonProcess; // Poisson process instance for generating inter-arrival times

    /**
     * Constructor for Peer.
     *
     * @param currentHost  Address of the current peer.
     * @param nextPeerHost Address of the next peer in the network.
     * @throws Exception If an error occurs during logger setup.
     */
    public Peer(String currentHost, String nextPeerHost) throws Exception {
        this.currentHost = currentHost;
        Random randomGenerator = new Random();
        double rateParameter = 3.0;

        // Set up logger
        logger = Logger.getLogger("Peer-" + currentHost);
        setupLogger();

        // Create PoissonProcess instance
        poissonProcess = new PoissonProcess(rateParameter, randomGenerator);
        logger.info(String.format("Peer initialized: currentHost=%s, nextPeerHost=%s", currentHost, nextPeerHost));
    }

    /**
     * Configures the logger with a file handler and formatter.
     *
     * @throws Exception If an error occurs during logger setup.
     */
    private void setupLogger() throws Exception {
        FileHandler fileHandler = new FileHandler("./" + currentHost + ".log", true);
        logger.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }

    public static void main(String[] args) {
        // Validate command-line arguments
        if (args.length < 4) {
            System.out.println("Error: Missing required arguments.");
            return;
        }

        String currentHost = args[0];
        int localPort;
        String nextPeerHost = args[2];
        int nextPeerPort;

        try {
            // Parse port numbers
            localPort = parsePort(args[1], "localPort");
            nextPeerPort = parsePort(args[3], "nextPeerPort");

            // Initialize the Peer instance
            Peer peer = new Peer(currentHost, nextPeerHost);
            peer.logger.info("Peer started at currentHost=" + currentHost);
            System.out.printf("New peer started at currentHost=%s\n", currentHost);

            // Initialize Server instance
            Server server = new Server(currentHost, localPort, peer.logger, nextPeerHost, nextPeerPort);
            new Thread(server).start();

            // Initialize RequestGenerator with the server
            RequestGenerator requestGenerator = new RequestGenerator(currentHost, peer.logger, peer.poissonProcess, localPort, server);
            new Thread(requestGenerator).start();
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses a port number from a string.
     *
     * @param portStr    The port number as a string.
     * @param portName   The name of the port (for logging purposes).
     * @return The parsed port number.
     * @throws IllegalArgumentException If the port number is invalid.
     */
    private static int parsePort(String portStr, String portName) {
        try {
            int port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException(portName + " must be between 1 and 65535.");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(portName + " must be a valid integer.", e);
        }
    }
}
