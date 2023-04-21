package sec.G31.client;
import java.util.HashSet;
import java.util.Set;

import sec.G31.messages.AckMessage;
import sec.G31.messages.DecidedMessage;
import org.apache.commons.lang3.*;
import java.io.*;
import java.net.*;

/**
 * Since it extends Thread, the procedure defined in run will run in a separate thread
 * from the rest of the program. 
 * When you want to run the run function and you call UDPserver.start
 */
public class UDPServerClient extends Thread{

	
	private static final int MAX_UDP_DATA_SIZE = (64 * 1024 - 1) - 8 - 20;
    private static Set<Class<? extends Serializable>> possibleClasses = new HashSet<>();
	/** Buffer size for receiving a UDP packet. */
	private static final int BUFFER_SIZE = MAX_UDP_DATA_SIZE;

    /** datagram socket for this server */
    private DatagramSocket _socket;
    private UDPChannelClient _UDPchannel;

    static {
        possibleClasses.add(DecidedMessage.class);
        possibleClasses.add(AckMessage.class);
    }

    /**
     * Creates the UDP server, links it to the udp channel that created it.
     * 
     */
	public UDPServerClient(UDPChannelClient channel, int portNumber, DatagramSocket socket) throws IOException{
        _UDPchannel = channel;
        _socket = socket;
	}

    /**
     * It will run in a new separate thread
     * class that will process the message received
    */
    class ProcessMessage implements Runnable{
            InetAddress _address;
            int _port;
            DecidedMessage _msg;
            public ProcessMessage(InetAddress clientAddress, int clientPort, DecidedMessage msg){
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
     * Runs in a new thread.
     * 
     */
    public void run(){
        // wait for client packets until end message is received
        try{
            boolean running = true;
            byte[] buf = new byte[BUFFER_SIZE];
            while (running) {
                DatagramPacket clientPacket = new DatagramPacket(buf, buf.length);
                _socket.receive(clientPacket);
            
                InetAddress clientAddress = clientPacket.getAddress();
                int clientPort = clientPacket.getPort();
                byte[] clientData = clientPacket.getData();
                
                // we don't know the class of the message we received a priori
                Object obj = this.deserialize(clientData);
                Class<?> c = obj.getClass();
                switch (c.getName()) {
                    case "sec.G31.messages.DecidedMessage":
                        DecidedMessage message = (DecidedMessage) obj;
                        Thread t1 = new Thread(new ProcessMessage(clientAddress, clientPort, message));
                        t1.start();
                        break;
                    case "sec.G31.messages.AckMessage":
                        AckMessage ackMessage = (AckMessage) obj;
                        _UDPchannel.receivedAck(ackMessage, clientPort);
                        break;                        
                    default:
                        break;
                }
            }
            // Close socket (this will also close the socket used by the client)
            _socket.close();
        } catch(Exception e){
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