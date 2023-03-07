package sec.G31;
import java.util.logging.Logger;
import java.net.*;

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

    public PerfectAuthChannel(Server server, InetAddress serverAddress, int serverPort){
        _server = server;
        _address = serverAddress;
        _port = serverPort;
        _stubChannel = new StubbornChannel(this, _address, _port);
    }

    public void sendMessage(InetAddress destAddress, int destPort, String msg){
        LOGGER.info("PAC:: " + destAddress + " " + destPort + " " + msg);
        _stubChannel.sendMessage(destAddress, destPort, msg);
    }

    public void receivedMessage(String msg, int port, InetAddress address){
        LOGGER.info("PAC:: received message");
        _server.receivedMessage(msg, port, address);
    }
}