package sec.G31;

import java.io.*;
import java.net.*;

public class PerfectAuthChannel 
{
    private StubbornChannel _stubChannel;
    private InetAddress _address; 
    private int _port;

    public PerfectAuthChannel(InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _port = serverPort;
        _stubChannel = new StubbornChannel(_address, _port);
    }

    public void sendMessage(String destAddress, int destPort, String msg){
        System.out.printf("%s %d %s\n", destAddress, destPort, msg);
        _stubChannel.sendMessage(destAddress, destPort, msg);
    }
}