package sec.G31;

import java.io.*;
import java.net.*;

public class Server
{
    private InetAddress _address;
    private int _port;
    private PerfectAuthChannel _channel;

    public Server(InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _port = serverPort;
        _channel = new PerfectAuthChannel(_address, _port);
    }

    public void sendMessage(String destServer, int destPort, String msg ){
        System.out.printf("%s %d %s\n", destServer, destPort, msg);
        _channel.sendMessage(destServer, destPort, msg);
    }

}