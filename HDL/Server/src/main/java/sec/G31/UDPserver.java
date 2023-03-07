package sec.G31;
import java.util.logging.Logger;
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

    private final static Logger LOGGER = Logger.getLogger(UDPserver.class.getName());

	/** Buffer size for receiving a UDP packet. */
	private static final int BUFFER_SIZE = MAX_UDP_DATA_SIZE;

	/** Custom command to stop the server. */
	private static final String END_MESSAGE = "end";

    /** port number of the server */
    private final int _port; 

    /** datagram socket for this server */
    private DatagramSocket _socket;

    private UDPchannel _channel;

    /**
     * Creates the UDP server, links it to the udp channel that created it.
     * 
     */
	public UDPserver(UDPchannel channel, int portNumber, DatagramSocket socket) throws IOException{
		_port = portNumber;
        _channel = channel;
        _socket = socket;
		//System.out.printf("Server will be connected to port %d %n", _port);
	}

    /**
     * It will run in a new separate thread
     * class that will process the message received
    */
    class ProcessMessage implements Runnable{
            InetAddress _address;
            int _port;
            String _msg;
            public ProcessMessage(InetAddress clientAddress, int clientPort, String txt){
                _address = clientAddress;
                _port = clientPort;
                _msg = txt;
            }

            /** it will run in a separate thread */
            public void run(){
                System.out.printf("UDP S:: %s %d %s\n", _address, _port, _msg);
                _channel.receivedMessage(_msg, _port, _address);
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
                int clientLength = clientPacket.getLength();
                byte[] clientData = clientPacket.getData();
            
                //System.out.printf("Received from: %s:%d %n", clientAddress, clientPort);
                LOGGER.info("Received from: " + clientAddress + ":" + clientPort);
                //System.out.printf("Received bytes: %d %n", clientLength);
                // this will usually be smaller than the buffer size
                //System.out.printf("Receive buffer size: %d %n", clientData.length);
            
                String clientText = new String(clientData, 0, clientLength);
                //System.out.println("Received text: " + clientText);
                LOGGER.info("Received text: " + clientText);
            
                if (END_MESSAGE.equals(clientText)) {
                    // this will be the last reply from the server
                    System.out.println("Received END message");
                    running = false;
                }

                // calling a new thread that will process the message
                Thread t1 = new Thread(new ProcessMessage(clientAddress, clientPort, clientText));
                t1.start();
            }

            // Close socket (this will also close the socket used by the client)
            _socket.close();
            LOGGER.info("Closed socket");
            //System.out.println("Closed socket");
        } catch( IOException e){
            System.out.println("Error on UDP server");
            LOGGER.log(java.util.logging.Level.SEVERE, "Error on UDP server");
            e.printStackTrace();
        }
    }
}