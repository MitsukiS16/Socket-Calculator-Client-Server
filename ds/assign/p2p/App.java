import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Main application to demonstrate the Poisson Process and Peer connections.
 * 
 * This application simulates a peer-to-peer network using the Poisson distribution to 
 * manage event timings (anti-entropy mechanism). Each peer communicates with other peers 
 * to share network information.
 */
public class App {

    // Constants for simulation
    private static final int POISSON_RATE = 5; // Rate parameter for Poisson process (events per minute)
    private static final int MAIN_APP_DURATION_MS = 20 * 1000; // Main application runtime (in milliseconds)

    public static void main(String[] args) {
        Peer peer = null; // Declare Peer instance for cleanup

        // Validate command-line arguments
        if (args.length < 3 || (args.length - 2) % 2 != 0) {
            System.out.println("Error: Provide at least one peer and ports in pairs:");
            System.out.println("<mainHost> <mainPort> <connectedHost> <connectedPort> ...");
            return;
        }

        try {
            // Parse main peer information
            String mainHost = args[0];
            int mainPort = Integer.parseInt(args[1]);

            // Initialize the map of connected peers
            Map<Map.Entry<String, Integer>, Integer> nextPeers = new HashMap<>();
            for (int i = 2; i < args.length; i += 2) {
                String peerHost = args[i];
                int peerPort = Integer.parseInt(args[i + 1]);
                nextPeers.put(new AbstractMap.SimpleEntry<>(peerHost, peerPort), 0);
            }

            // Initialize and start the Peer
            peer = new Peer(mainHost, mainPort, nextPeers);
            Thread peerThread = new Thread(peer);
            peerThread.start();

            // Simulate the Poisson Process
            PoissonProcess poissonProcess = new PoissonProcess(POISSON_RATE, new Random());
            Thread poissonThread = new Thread(() -> {
                try {
                    while (true) {
                        double timeToNextEvent = poissonProcess.calculate(0);
                        System.out.printf("Time to Next Event: %.4f seconds%n", timeToNextEvent);
                        Thread.sleep((long) (timeToNextEvent * 1000));
                    }
                } catch (InterruptedException e) {
                    System.out.println("Poisson Loop interrupted, exiting.");
                }
            });
            poissonThread.start();
            System.out.println("Poisson Loop Initiated");

            // Shutdown hook to ensure cleanup
            Peer finalPeer = peer;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown signal received, closing peer...");
                finalPeer.close();
            }));

            // Run the application for the specified duration
            Thread.sleep(MAIN_APP_DURATION_MS);

            // Gracefully stop threads
            poissonThread.interrupt();
            peerThread.interrupt();
            
            try {
                poissonThread.join(); // Wait for the Poisson thread to finish
                peerThread.join();    // Wait for the Peer thread to finish
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for threads to finish.");
            }
            
            peer.close();
            System.out.println("Program finished, exiting.");

        } catch (Exception e) {
            System.err.println("Error initializing peer or Poisson process: " + e.getMessage());
            if (peer != null) {
                peer.close();
            }
        }
    }
}
