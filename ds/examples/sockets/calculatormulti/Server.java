package ds.examples.sockets.calculatormulti;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket server;

    public Server(String ipAddress) throws Exception {
        this.server = new ServerSocket(0, 1, InetAddress.getByName(ipAddress));
    }

    private void listen() throws Exception {
		while(true) {
			Socket client = this.server.accept();
			String clientAddress = client.getInetAddress().getHostAddress();
			System.out.printf("\r\nNew connection from %s\n", clientAddress);
			new Thread(new ConnectionHandler(clientAddress, client)).start();
		}
    }
    
    public InetAddress getSocketAddress() {
		return this.server.getInetAddress();
    }
        
    public int getPort() {
		return this.server.getLocalPort();
    }
    
    public static void main(String[] args) throws Exception {
		Server app = new Server(args[0]);
		System.out.printf("\r\nrunning server: host=%s @ port=%d\n",
			app.getSocketAddress().getHostAddress(), app.getPort());
		app.listen();
    }
}



class ConnectionHandler implements Runnable {
    String clientAddress;
    Socket clientSocket;    

    public ConnectionHandler(String clientAddress, Socket clientSocket) {
		this.clientAddress = clientAddress;
		this.clientSocket  = clientSocket;    
    }

    @Override
    public void run() {
		// Prepare socket I/O channels
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));    
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		
			while(true) {
				// Receive command 
				String command;
				if( (command = in.readLine()) == null)
					break;
				else
					System.out.printf("Message from %s : %s\n", clientAddress, command);		  
					
				Scanner sc = new Scanner(command).useDelimiter(":");
				String  op = sc.next();
				String result = "";
				String s = "";
				String s1 = "";

				switch(op) {
					case "len": 
						s = sc.next();
						result = String.valueOf(length(s));
						break;
					case "equal": 
						s = sc.next();
						s1 = sc.next();
						result = String.valueOf(equal(s,s1));
						break;
					case "cat": 
						s = sc.next();
						s1 = sc.next();
						result = cat(s,s1); 
						break;
					case "break": 
						s = sc.next();
						char d = sc.next().charAt(0);
						result = String.join(", ",breakString(s,d));
						break;
					default:
						result = "Unknown";
						break;
				}  
				/*
				* send result
				*/
				out.println(String.valueOf(result));
				out.flush();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

	private int length(String str) {
        return str.length();
    }

    private boolean equal(String str1, String str2) {
        return str1.equals(str2);
    }

    private String cat(String str1, String str2) {
        return str1 + str2;
    }

    private String[] breakString(String str, char delimiter) {
        return str.split(Character.toString(delimiter));
    }
}
