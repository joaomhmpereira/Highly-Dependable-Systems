package sec.G31;

import java.io.*;
import java.net.*;

public class StubbornChannel
{
    private UDPchannel _udpChannel;
    private InetAddress _address; 
    private int _port;
    private PerfectAuthChannel _pac;

    public StubbornChannel(PerfectAuthChannel pac, InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _port = serverPort;
        _udpChannel = new UDPchannel(this, _address, _port);
        _pac = pac;
    }

    public void sendMessage(InetAddress destAddress, int destPort, String msg){
        System.out.printf("SC:: %s %d %s\n", destAddress, destPort, msg);
        _udpChannel.sendMessage(destAddress, destPort, msg);
    }

    public void receivedMessage(String msg, int port, InetAddress address){
        System.out.println("UDP:: received message");
        _pac.receivedMessage(msg, port, address);
    }
}