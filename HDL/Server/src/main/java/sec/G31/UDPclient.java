package sec.G31;

import java.io.*;
import java.net.*;

public class UDPclient
{
    /** Buffer size for receiving a UDP packet. */
	private static final int BUFFER_SIZE = 65_507;

    // input: "hostaddress" "hostport" msg 

	public UDPclient(InetAddress serverAddress, int port, DatagramSocket socket, String msg) throws IOException {
		final String clientText = msg; // text to send

		// Text is sent to server as bytes
		byte[] clientBuffer = clientText.getBytes();
		System.out.printf("%d bytes to send%n", clientBuffer.length);

		DatagramPacket clientPacket = new DatagramPacket(clientBuffer, clientBuffer.length, serverAddress, port);
		System.out.printf("Send to: %s:%d %n", serverAddress.toString(), port);
		socket.send(clientPacket);
		System.out.println("Sent packet: " + clientPacket.toString());

		System.out.println("Socket closed");
	}

}