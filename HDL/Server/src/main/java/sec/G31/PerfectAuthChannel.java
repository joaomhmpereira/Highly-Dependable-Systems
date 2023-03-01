package sec.G31;


public class PerfectAuthChannel 
{
    private StubbornChannel _stubChannel;
    private InetAddress _address; 
    private int _port;

    public PerfectAuthChannel(InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        StubbornChannel _stubChannel = new StubbornChannel(_address);
        _port = serverPort;
    }
}