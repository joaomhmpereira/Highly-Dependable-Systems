package sec.G31;

import java.io.*;
import java.net.*;

public class StubbornChannel
{
    private UDPchannel _udpChannel;
    private InetAddress _address; 
    private int _port;

    public StubbornChannel(InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _port = serverPort;
        _udpChannel = new UDPchannel(_address, _port);
    }

    public void sendMessage(String destAddress, int destPort, String msg){
        System.out.printf("%s %d %s\n", destAddress, destPort, msg);
        _udpChannel.sendMessage(destAddress, destPort, msg);
    }
}