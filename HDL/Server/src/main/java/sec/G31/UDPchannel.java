package sec.G31;


public class UDPchannel 
{
    private InetAddress _address;
    private int _port;

    public UDPchannel(InetAddress address, int serverPort){
        _address = address;
        _port = serverPort;
        
    }

}