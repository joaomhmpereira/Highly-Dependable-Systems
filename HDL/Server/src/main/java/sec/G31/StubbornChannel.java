package sec.G31;

/**
 * This class will implement the creation of the message to send
 * it will have 1 module named perfect channel.
 *
 */
public class StubbornChannel
{
    private UDPchannel _udpchannel;
    private InetAddress _address; 
    private int _port

    public PerfectAuthChannel(InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _udpchannel = new UDPchannel(_address);
        _port = serverPort;
    }
}