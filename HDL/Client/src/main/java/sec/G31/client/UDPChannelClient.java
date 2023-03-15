package sec.G31.client;
//import java.util.logging.Logger;

import sec.G31.messages.AckMessage;
import sec.G31.messages.DecidedMessage;
import sec.G31.messages.Message;
import java.io.*;
import java.net.*;

public class UDPChannelClient 
{
    //private final static Logger LOGGER = Logger.getLogger(UDPChannelClient.class.getName());
    private StubbornClient _STUBchannel;
    private UDPServerClient _server;
    private DatagramSocket _socket;

    public UDPChannelClient(StubbornClient channel, InetAddress address, int serverPort){
        try{
            _STUBchannel = channel;
            _socket = new DatagramSocket(serverPort);
            _server = new UDPServerClient(this, serverPort, _socket);
            _server.start();
        }catch(IOException e){
            System.out.println("Error while creating UDP server");
            e.printStackTrace();
        }
    }

    public void sendMessage(InetAddress destAddress, int destPort, Message msg){
        try{
            //LOGGER.info("UDPchannel:: " + destAddress + " " + destPort + " ::: " + msg.toString());
           new UDPClientClient(destAddress, destPort, _socket, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

    public void sendAck(InetAddress destAddress, int destPort, AckMessage msg){
        try{
            //LOGGER.info("UDPchannel:: " + destAddress + " " + destPort + " ::: " + msg.toString());
           new UDPClientClient(destAddress, destPort, _socket, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

    public void receivedAck(AckMessage msg, int port){
        _STUBchannel.receivedAck(msg, port);
    }

    public void receivedMessage(DecidedMessage msg, int port, InetAddress address){
        //System.out.println("UDP:: received message");
        AckMessage ack = new AckMessage(msg, port);
        this.sendAck(address, port, ack);
        _STUBchannel.receivedMessage(msg, port, address);
    }

}