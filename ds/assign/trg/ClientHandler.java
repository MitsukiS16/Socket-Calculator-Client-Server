

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Handles client connections in a separate thread.
 */
class ClientHandler implements Runnable {
    private final String clientIP;
    private final Socket clientSocket;
    private final Logger clientLogger;

    /**
     * Constructor to initialize the client handler.
     *
     * @param clientIP     The IP address of the client.
     * @param clientSocket The client socket.
     * @param logger       Logger for logging messages.
     */
    public ClientHandler(String clientIP, Socket clientSocket, Logger logger) {
        this.clientIP = clientIP;
        this.clientSocket = clientSocket;
        this.clientLogger = logger;
    }

    @Override
    public void run() {
        try (
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            clientLogger.info("Handling client at " + clientIP);

            String command;
            while ((command = inputReader.readLine()) != null) {
                clientLogger.info("Received command from " + clientIP + ": " + command);
                System.out.printf("Command from %s: %s%n", clientIP, command);

                // Process the command and send the response
                String response = processCommand(command);
                outputWriter.println(response);
                clientLogger.info("Response sent to " + clientIP + ": " + response);
            }

        } catch (Exception e) {
            clientLogger.severe("Error handling client " + clientIP + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                clientLogger.warning("Failed to close socket for client " + clientIP + ": " + e.getMessage());
            }
        }
    }

    /**
     * Processes the arithmetic command sent by the client.
     *
     * @param command The command string (format: operation:x:y).
     * @return The result of the operation or an error message.
     */
    private String processCommand(String command) {
        try (Scanner scanner = new Scanner(command).useDelimiter(":")) {
            String operation = scanner.next();
            double operand1 = scanner.nextDouble();
            double operand2 = scanner.nextDouble();
            double result;

            switch (operation) {
                case "add":
                    result = operand1 + operand2;
                    break;
                case "sub":
                    result = operand1 - operand2;
                    break;
                case "mul":
                    result = operand1 * operand2;
                    break;
                case "div":
                    if (operand2 == 0) {
                        return "Error: Division by zero.";
                    }
                    result = operand1 / operand2;
                    break;
                default:
                    return "Error: Unknown operation '" + operation + "'. Supported operations are add, sub, mul, div.";
            }

            return String.format("Result: %.2f", result);
        } catch (Exception e) {
            return "Error: Invalid command format. Use format 'operation:operand1:operand2'.";
        }
    }
}
