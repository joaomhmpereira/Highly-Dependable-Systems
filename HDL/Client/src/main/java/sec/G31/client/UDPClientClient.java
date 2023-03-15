package sec.G31.client;

import java.io.*;
import java.net.*;

import org.apache.commons.lang3.SerializationUtils;

import sec.G31.messages.AckMessage;
import sec.G31.messages.Message;

public class UDPClientClient
{

	public UDPClientClient(InetAddress serverAddress, int port, DatagramSocket socket, Message msg) throws IOException {

		// Text is sent to server as bytes
		byte[] clientBuffer = SerializationUtils.serialize(msg);

		DatagramPacket clientPacket = new DatagramPacket(clientBuffer, clientBuffer.length, serverAddress, port);
		socket.send(clientPacket);

	}

	public UDPClientClient(InetAddress serverAddress, int port, DatagramSocket socket, AckMessage msg) throws IOException {

		// Text is sent to server as bytes
		byte[] clientBuffer = SerializationUtils.serialize(msg);

		DatagramPacket clientPacket = new DatagramPacket(clientBuffer, clientBuffer.length, serverAddress, port);
		socket.send(clientPacket);

	}

}