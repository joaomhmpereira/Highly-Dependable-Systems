package sec.G31;

import java.io.*;
import java.net.*;

public class UDPchannel 
{
    private StubbornChannel _channel;
    private UDPclient _client;
    private UDPserver _server;
    private DatagramSocket _socket;

    public UDPchannel(StubbornChannel channel, InetAddress address, int serverPort){
        try{
            _channel = channel;
            _socket = new DatagramSocket(serverPort);
            _server = new UDPserver(this, serverPort, _socket);
            _server.start();
        }catch(IOException e){
            System.out.println("Error while creating UDP server");
            e.printStackTrace();
        }
    }

    public void sendMessage(InetAddress destAddress, int destPort, String msg){
        try{
            System.out.println("UDPchannel:: " + destAddress + " " + destPort + " " + msg);
            _client = new UDPclient(destAddress, destPort, _socket, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

    public void receivedMessage(String text, int port, InetAddress address){
        System.out.println("UDP:: received message");
        _channel.receivedMessage(text, port, address);
    }

}