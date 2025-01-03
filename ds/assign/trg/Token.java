

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * TokenSender is responsible for sending tokens to a specified host and port.
 */
public class Token {
    private static final Logger logger = Logger.getLogger(Token.class.getName());

    private static final int MAX_RETRIES = 5; // Maximum retry attempts
    private static final int SOCKET_TIMEOUT_MS = 1000; // Timeout for socket connection (1 second)

    /**
     * Sends a token to the specified host and port with retry logic.
     *
     * @param targetHost The target host.
     * @param targetPort The target port.
     * @param token      The token to send.
     */
    public static void sendToken(String targetHost, int targetPort, String token) {
        int attempt = 0;
        boolean isSuccess = false;

        while (attempt < MAX_RETRIES && !isSuccess) {
            attempt++;
            try (Socket socket = new Socket(targetHost, targetPort);
                 OutputStream outputStream = socket.getOutputStream();
                 PrintWriter printWriter = new PrintWriter(outputStream, true)) {

                // Send the token
                printWriter.println(token);
                printWriter.flush();

                logger.info(String.format("Token '%s' successfully sent to %s:%d on attempt %d/%d",
                        token, targetHost, targetPort, attempt, MAX_RETRIES));
                isSuccess = true;

            } catch (IOException e) {
                logger.warning(String.format(
                        "Failed to send token to %s:%d. Attempt %d/%d. Error: %s",
                        targetHost, targetPort, attempt, MAX_RETRIES, e.getMessage()));

                // Wait before retrying
                try {
                    Thread.sleep(SOCKET_TIMEOUT_MS);
                } catch (InterruptedException ie) {
                    logger.severe("Retry sleep interrupted: " + ie.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (!isSuccess) {
            logger.severe(String.format("Failed to send token to %s:%d after %d attempts.",
                    targetHost, targetPort, MAX_RETRIES));
        }
    }

    /**
     * Main method for sending tokens using command-line arguments.
     *
     * @param args Command-line arguments: <host> <port> <token>
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Error: Missing required arguments.");
            return;
        }
    
        String targetHost = args[0];  // Host as the first argument
        String portString = args[1];  // Port as the second argument (string format)
        String token = args[2];  // Token as the third argument
    
        int targetPort;  // Port will be an integer
    
        // Validate and parse the port number
        try {
            targetPort = Integer.parseInt(portString);
            if (targetPort < 1 || targetPort > 65535) {
                throw new IllegalArgumentException("Port number must be between 1 and 65535.");
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid port number: " + portString);
            return;
        }
    
        // Log the operation
        logger.info(String.format("Preparing to send token '%s' to %s:%d", token, targetHost, targetPort));
    
        // Send the token
        sendToken(targetHost, targetPort, token);
    
        if (logger.isLoggable(java.util.logging.Level.INFO)) {
            System.out.println("Operation complete. Check logs for details.");
        }
    }
    
    
}
