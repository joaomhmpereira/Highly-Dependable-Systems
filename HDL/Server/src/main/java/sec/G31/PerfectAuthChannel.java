package sec.G31;
import java.util.logging.Logger;
import java.net.*;
import sec.G31.messages.Message;
import java.util.Hashtable;


/**
 * TO-DO: implementar a autenticacao
 */
public class PerfectAuthChannel 
{
    private final static Logger LOGGER = Logger.getLogger(PerfectAuthChannel.class.getName());
    private StubbornChannel _stubChannel;
    private Server _server;
    private InetAddress _address; 
    private int _port;
    private BroadcastManager _broadcastManager;
    private Hashtable<Integer, Integer> _broadcastNeighbors; // to send broadcast

    public PerfectAuthChannel(BroadcastManager broadcastManager, Server server, InetAddress serverAddress, int serverPort,
                    Hashtable<Integer, Integer> broadcastNeighbours){
        _server = server;
        _address = serverAddress;
        _port = serverPort;
        _stubChannel = new StubbornChannel(this, _address, _port);
        _broadcastManager = broadcastManager;
        _broadcastNeighbors = broadcastNeighbours;
    }

    public void sendMessage(InetAddress destAddress, int destPort, Message msg){
        LOGGER.info("PAC:: " + destAddress + " " + destPort + " " + msg);
        _stubChannel.sendMessage(destAddress, destPort, msg);
    }

    public void receivedMessage(Message msg, int port, InetAddress address){
        LOGGER.info("PAC:: received message");

        // verify that it has came from the correct port 
        if(_broadcastNeighbors.get(msg.getSenderId()) == port)
            _broadcastManager.receivedMessage(msg);         // inform the upper layer 
    }

    public void setBroadcastManager(BroadcastManager manager){
        _broadcastManager = manager;
    }

}