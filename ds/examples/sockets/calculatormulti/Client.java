package ds.examples.sockets.calculatormulti;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private Scanner scanner;
    
    private Client(InetAddress serverAddress, int serverPort) throws Exception {
	this.socket  = new Socket(serverAddress, serverPort);
	this.scanner = new Scanner(System.in);
    }

    /*
     * send messages such as:
     *   - len:s
     *   - equal:s:s1 
     *   - cat:s:s1
     *   - break:s:d
     * where s and s1 are string
     * where d is a char delimiter
     *    */
    
    private void start() throws IOException {
        String command;
        String result;

        PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));        
        
        while (true) {
            // Read command
            System.out.print("$ ");
            command = scanner.nextLine();
            if(command.equals("quit")) break;
            
            // Send command
            out.println(command);
            out.flush();
            
            // Receive result
            result = in.readLine();
            


            try {
                double numericResult = Double.parseDouble(result);
                System.out.printf("Result: %f\n", numericResult);
            } catch (NumberFormatException e) {
                // If result isn't a number, print it as a string
                System.out.printf("Result: %s\n", result);
            }
        }

        // Close resources
        out.close();
        in.close();
        socket.close();
    }
            
    public static void main(String[] args) throws Exception {
        Client client = new Client(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
        System.out.printf("\r\nConnected to server: %s\n", client.socket.getInetAddress());
        client.start();                
    }
}
