import java.util.Arrays;
import java.util.List;

/**
 * The App class serves as the main entry point for the peer-to-peer network application.
 * It initializes a Peer instance, connects it to other peers, and runs the application
 * for a specified duration.
 */
public class App {

    // The runtime duration for the main application (in milliseconds)
    private static final int MAIN_APP_DURATION_MS = 20 * 1000;

    // List of all peer ports in the network
    private static final List<Integer> ALL_PEERS = Arrays.asList(5000, 5001, 5002, 5003, 5004, 5005);

    /**
     * The main method is the entry point for the application.
     *
     * @param args Command-line arguments: host (String) and port (int) for the Peer.
     */
    public static void main(String[] args) {
        Peer peer = null; // Declare Peer instance for cleanup

        // Validate command-line arguments
        if (args.length < 2) {
            System.out.println("Error: Provide host and port as arguments.");
            return;
        }

        try {
            // Parse main peer information
            String host = args[0]; // The host address (e.g., localhost)
            int port = Integer.parseInt(args[1]); // The port number for this Peer

            // Initialize and configure the Peer
            peer = new Peer(host, port);

            // Add all peers except itself
            for (int otherPort : ALL_PEERS) {
                if (otherPort != port) {
                    peer.addPeer(host, otherPort); // Connect to other peers on the same host
                }
            }

            // Start the Peer in a separate thread
            Thread peerThread = new Thread(peer);
            peerThread.start();
            System.out.println("Starting peer connection...");

            // Run the application for the specified duration
            Thread.sleep(MAIN_APP_DURATION_MS);

            // Gracefully stop threads
            peer.stop(); // Signal the Peer to stop
            peerThread.join(); // Wait for the Peer thread to finish

            System.out.println("Program finished, exiting.");

        } catch (Exception e) {
            // Handle initialization or runtime errors
            System.err.println("Error initializing peer or Poisson process: " + e.getMessage());
        } finally {
            // Ensure resources are cleaned up
            if (peer != null) {
                peer.close(); // Close any open connections
            }
        }
    }
}
