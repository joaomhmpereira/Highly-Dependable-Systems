package sec.G31;


public class Server
{
    private InetAddress _address;
    private int _port;
    private PerfectAuthChannel _channel;

    public Server(InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _channel = new PerfectAuthChannel(_address);
        _port = serverPort;
    }

}