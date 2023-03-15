package sec.G31;

import sec.G31.messages.*;
import java.io.*;
import java.net.*;

public class UDPchannel 
{
    private StubbornChannel _stubChannel;
    private UDPserver _server;
    private DatagramSocket _socket;

    public UDPchannel(StubbornChannel channel, InetAddress address, int serverPort){
        try{
            _stubChannel = channel;
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
            //LOGGER.info("UDPchannel:: " + destAddress + " " + destPort + " ::: " + msg.toString());
            new UDPclient(destAddress, destPort, _socket, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

    public void sendDecide(InetAddress destAddress, int destPort, DecidedMessage msg){
        try{
            //LOGGER.info("UDPchannel:: " + destAddress + " " + destPort + " ::: " + msg.toString());
            new UDPclient(destAddress, destPort, _socket, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

    public void sendAck(InetAddress destAddress, int destPort, AckMessage msg){
        try{
            //LOGGER.info("UDPchannel:: " + destAddress + " " + destPort + " ::: " + msg.toString());
            new UDPclient(destAddress, destPort, _socket, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

    /**
     * decided wheter to send a Ack Decided or a Ack message
     */
    public void receivedAck(AckMessage msg, int port){
        if(msg.isDecidedMessage())
            _stubChannel.receivedAckDecided(msg, port);
        else
            _stubChannel.receivedAck(msg, port);
    }

    public void receivedMessage(Message msg, int port, InetAddress address){
        //LOGGER.info("UDP:: received message");
        AckMessage ack = new AckMessage(msg, port);
        this.sendAck(address, port, ack);
        _stubChannel.receivedMessage(msg, port, address);

    }

}