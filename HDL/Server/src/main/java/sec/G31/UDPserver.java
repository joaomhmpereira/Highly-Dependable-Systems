package sec.G31;
//import java.util.logging.Logger;

import sec.G31.messages.*;
import org.apache.commons.lang3.*;
import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Since it extends Thread, the procedure defined in run will run in a separate thread
 * from the rest of the program. 
 * When you want to run the run function and you call UDPserver.start
 */
public class UDPserver extends Thread{

	private static final int MAX_UDP_DATA_SIZE = (64 * 1024 - 1) - 8 - 20;
	/** Buffer size for receiving a UDP packet. */
	private static final int BUFFER_SIZE = MAX_UDP_DATA_SIZE;
    private static Set<Class<? extends Serializable>> possibleClasses = new HashSet<>();
    /** datagram socket for this server */
    private DatagramSocket _socket;
    private UDPchannel _UDPchannel;

    static {
        possibleClasses.add(Message.class);
        possibleClasses.add(AckMessage.class);
    }

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
        
                try { 
                    // we don't know the class of the message we received a priori
                    Object obj = deserialize(clientData);
                    Class<?> c = obj.getClass();
                    switch (c.getName()) {
                        case "sec.G31.messages.Message":
                            Message message = (Message) obj;
                            Thread t1 = new Thread(new ProcessMessage(clientAddress, clientPort, message));
                            t1.start();
                            break;
                        case "sec.G31.messages.AckMessage":
                            AckMessage ackMessage = (AckMessage) obj;
                            Thread t2 = new Thread(new ProcessAck(clientPort, ackMessage));
                            t2.start();
                            break;                        
                        default:
                            break;
                    }
                    
                } catch (ClassNotFoundException e) { // if we receive a ack to a message
                    e.printStackTrace();
                }
                
            }

            // Close socket (this will also close the socket used by the client)
            _socket.close();
        } catch( IOException e){
            System.out.println("Error on UDP server");
            e.printStackTrace();
        }
    }

    /**
     * Deserializes a byte array to an object of one of the possible classes.
     * If the deserialization fails, it tries the next class in the list.
     * @param <T>
     * @param data
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T deserialize(byte[] data) throws ClassNotFoundException {
        for (Class<? extends Serializable> clazz : possibleClasses) {
            try {
                Serializable obj = SerializationUtils.deserialize(data);
                if (clazz.isInstance(obj)) {
                    return (T) obj;
                }
            } catch (Exception e) {
                // Deserialization failed, try next class
            }
        }
        throw new ClassNotFoundException("Failed to deserialize object to any known class");
    }
}