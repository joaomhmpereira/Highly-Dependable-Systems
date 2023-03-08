package sec.G31;
import java.util.logging.Logger;

import sec.G31.messages.Message;
import java.io.*;
import java.net.*;

public class UDPchannel 
{
    private final static Logger LOGGER = Logger.getLogger(UDPchannel.class.getName());
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

    public void sendMessage(InetAddress destAddress, int destPort, Message msg){
        try{
            LOGGER.info("UDPchannel:: " + destAddress + " " + destPort + " ::: " + msg.toString());
            _client = new UDPclient(destAddress, destPort, _socket, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

    public void receivedMessage(Message msg, int port, InetAddress address){
        LOGGER.info("UDP:: received message");
        _channel.receivedMessage(msg, port, address);
    }

}