package sec.G31;

import java.io.*;
import java.net.*;

import org.apache.commons.lang3.SerializationUtils;

import sec.G31.messages.InitInstance;

public class UDPClientClient
{

	public UDPClientClient(InetAddress serverAddress, int port, DatagramSocket socket, InitInstance msg) throws IOException {

		// Text is sent to server as bytes
		byte[] clientBuffer = SerializationUtils.serialize(msg);
		//System.out.printf("%d bytes to send%n", clientBuffer.length);

		DatagramPacket clientPacket = new DatagramPacket(clientBuffer, clientBuffer.length, serverAddress, port);
		//System.out.printf("Send to: %s:%d %n", serverAddress.toString(), port);
		socket.send(clientPacket);
		//System.out.println("Sent packet: " + clientPacket.toString());

		//System.out.println("Socket closed");
	}

}