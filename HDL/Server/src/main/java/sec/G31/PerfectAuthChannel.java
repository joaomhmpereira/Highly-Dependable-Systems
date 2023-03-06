package sec.G31;

import java.io.*;
import java.net.*;

public class PerfectAuthChannel 
{
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
        System.out.printf("PAC:: %s %d %s\n", destAddress, destPort, msg);
        _stubChannel.sendMessage(destAddress, destPort, msg);
    }

    public void receivedMessage(String msg, int port, InetAddress address){
        System.out.println("UDP:: received message");

        _server.receivedMessage(msg, port, address);
    }
}