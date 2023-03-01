package sec.G31;

import java.io.*;
import java.net.*;

public class UDPclient
{
    /** Buffer size for receiving a UDP packet. */
	private static final int BUFFER_SIZE = 65_507;

    // input: "hostaddress" "hostport" msg 

	public UDPclient(String hostname, int port, String msg) throws IOException {
		// First argument is the server host name
		final String serverHost = hostname; // can be "127.0.0.1"
		// Second argument is the server port
		// Convert port from String to int
		final int serverPort = port;
		final InetAddress serverAddress = InetAddress.getByName(serverHost); 

		final String clientText = msg;

		// Create socket (we are not specifying a client port but we could)
		DatagramSocket socket = new DatagramSocket(); // we just uses one that is available to send the message

		// Text is sent to server as bytes
		byte[] clientBuffer = clientText.getBytes();
		System.out.printf("%d bytes to send%n", clientBuffer.length);

		DatagramPacket clientPacket = new DatagramPacket(clientBuffer, clientBuffer.length, serverAddress, serverPort);
		System.out.printf("Send to: %s:%d %n", serverHost, serverPort);
		socket.send(clientPacket);
		System.out.println("Sent packet: " + clientPacket);

		byte[] serverBuffer = new byte[BUFFER_SIZE];
		DatagramPacket serverPacket = new DatagramPacket(serverBuffer, serverBuffer.length);
		System.out.println("Wait for packet to arrive...");
		socket.receive(serverPacket);

		System.out.println("Received packet: " + serverPacket);
		String serverText = new String(serverPacket.getData(), 0, serverPacket.getLength());
		System.out.println("Received text: " + serverText);

		socket.close();
		System.out.println("Socket closed");
	}

}