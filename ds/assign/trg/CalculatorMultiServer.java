

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Multi-threaded Calculator Server.
 * Listens for client connections, processes arithmetic operations, and sends back results.
 */
public class CalculatorMultiServer {
    private final ServerSocket serverSocket;
    private final Logger serverLogger;

    /**
     * Constructor to initialize the server.
     *
     * @param ip        The IP address the server binds to.
     * @param port      The port the server listens on.
     * @throws Exception if the server cannot be initialized.
     */
    public CalculatorMultiServer(String ip, int port) throws Exception {
        this.serverSocket = new ServerSocket(port, 1, InetAddress.getByName(ip));
        this.serverLogger = Logger.getLogger("Calculator Multi Server");

        // Configure logger with file handler
        FileHandler fileHandler = new FileHandler("calculator.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        serverLogger.addHandler(fileHandler);

        serverLogger.info(String.format("Server initialized at %s:%d", ip, port));
    }

    /**
     * Listens for incoming client connections and starts a thread for each.
     *
     * @throws Exception if an error occurs while listening.
     */
    private void listenForClients() throws Exception {
        serverLogger.info("Server started. Waiting for client connections...");
        System.out.println("Server is running. Waiting for client connections...");

        while (true) {
            Socket clientSocket = this.serverSocket.accept();
            String clientIP = clientSocket.getInetAddress().getHostAddress();

            serverLogger.info("New connection established with " + clientIP);
            System.out.printf("New connection from %s%n", clientIP);

            // Start a new thread to handle the client connection
            new Thread(new ClientHandler(clientIP, clientSocket, serverLogger)).start();
        }
    }

    public InetAddress getServerAddress() {
        return this.serverSocket.getInetAddress();
    }

    public int getServerPort() {
        return this.serverSocket.getLocalPort();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Error: Missing required arguments.");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        CalculatorMultiServer serverApp = new CalculatorMultiServer(host, port);
        System.out.printf("Server running at %s:%d%n", serverApp.getServerAddress().getHostAddress(), serverApp.getServerPort());

        serverApp.listenForClients();
    }
}
