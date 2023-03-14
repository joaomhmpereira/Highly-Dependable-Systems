package sec.G31;
//import java.util.logging.Logger;

import sec.G31.messages.DecidedMessage;
import sec.G31.messages.InitInstance;
//import sec.G31.messages.Message;
import java.io.*;
import java.net.*;

public class UDPChannelClient 
{
    //private final static Logger LOGGER = Logger.getLogger(UDPChannelClient.class.getName());
    private StubbornClient _channel;
    private UDPClientClient _client;
    private UDPServerClient _server;
    private DatagramSocket _socket;

    public UDPChannelClient(StubbornClient channel, InetAddress address, int serverPort){
        try{
            _channel = channel;
            _socket = new DatagramSocket(serverPort);
            _server = new UDPServerClient(this, serverPort, _socket);
            _server.start();
        }catch(IOException e){
            System.out.println("Error while creating UDP server");
            e.printStackTrace();
        }
    }

    public void sendMessage(InetAddress destAddress, int destPort, InitInstance msg){
        try{
            //LOGGER.info("UDPchannel:: " + destAddress + " " + destPort + " ::: " + msg.toString());
            _client = new UDPClientClient(destAddress, destPort, _socket, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

    public void receivedMessage(DecidedMessage msg, int port, InetAddress address){
        //LOGGER.info("UDP:: received message");
        _channel.receivedMessage(msg, port, address);
    }

}