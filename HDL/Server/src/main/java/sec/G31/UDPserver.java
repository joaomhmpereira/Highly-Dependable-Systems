package sec.G31;
//import java.util.logging.Logger;

import sec.G31.messages.*;
import org.apache.commons.lang3.*;
import java.io.*;
import java.net.*;

/**
 * Since it extends Thread, the procedure defined in run will run in a separate thread
 * from the rest of the program. 
 * When you want to run the run function and you call UDPserver.start
 */
public class UDPserver extends Thread{

	/**
	 * Maximum size for a UDP packet. The field size sets a theoretical limit of
	 * 65,535 bytes (8 byte header + 65,527 bytes of data) for a UDP datagram.
	 * However the actual limit for the data length, which is imposed by the IPv4
	 * protocol, is 65,507 bytes (65,535 − 8 byte UDP header − 20 byte IP header.
	 */
	private static final int MAX_UDP_DATA_SIZE = (64 * 1024 - 1) - 8 - 20;

    //private final static Logger LOGGER = Logger.getLogger(UDPserver.class.getName());

	/** Buffer size for receiving a UDP packet. */
	private static final int BUFFER_SIZE = MAX_UDP_DATA_SIZE;

    /** datagram socket for this server */
    private DatagramSocket _socket;

    private UDPchannel _UDPchannel;

    /**
     * Creates the UDP server, links it to the udp channel that created it.
     * 
     */
	public UDPserver(UDPchannel channel, int portNumber, DatagramSocket socket) throws IOException{
        _UDPchannel = channel;
        _socket = socket;
		//System.out.printf("Server will be connected to port %d %n", _port);
	}

    /**
     * Processes a received message
     */
    class ProcessMessage implements Runnable{
        InetAddress _address;
        int _port;
        Message _msg;
        
        public ProcessMessage(InetAddress clientAddress, int clientPort, Message msg){
            _address = clientAddress;
            _port = clientPort;
            _msg = msg;
        }
        
        /** it will run in a separate thread */
        public void run(){
            //System.out.printf("UDP S:: %s %d %s\n", _address, _port, _msg.toString());
            _UDPchannel.receivedMessage(_msg, _port, _address);
        }
    }

    /**
     * Processes a received Ack message
     */
    class ProcessAck implements Runnable{
        AckMessage _msg;
        int _port;
        
        public ProcessAck(int clientPort, AckMessage msg){
            _port = clientPort;
            _msg = msg;
        }
        
        /** it will run in a separate thread */
        public void run(){
            _UDPchannel.receivedAck(_msg, _port);
        }
    }

    /**
     * Runs in a new thread.
     * 
     * TO-DO: the part of processing the received thread should be in a new thread.
     */
    public void run(){
        // wait for client packets until end message is received
        try{
            boolean running = true;
            byte[] buf = new byte[BUFFER_SIZE];
            while (running) {
                DatagramPacket clientPacket = new DatagramPacket(buf, buf.length);
                _socket.receive(clientPacket);
                //System.out.println("Received packet: " + clientPacket);
            
                InetAddress clientAddress = clientPacket.getAddress();
                int clientPort = clientPacket.getPort();
                byte[] clientData = clientPacket.getData();
        
                try { // if we receive a normal message
                    Message message = (Message) SerializationUtils.deserialize(clientData);
                    Thread t1 = new Thread(new ProcessMessage(clientAddress, clientPort, message));
                    t1.start();

                } catch (ClassCastException e) { // if we receive a ack to a message
                    AckMessage ackMessage = (AckMessage) SerializationUtils.deserialize(clientData);
                    Thread t1 = new Thread(new ProcessAck(clientPort, ackMessage));
                    t1.start();
                }
                
            }

            // Close socket (this will also close the socket used by the client)
            _socket.close();
        } catch( IOException e){
            System.out.println("Error on UDP server");
            e.printStackTrace();
        }
    }
}