package sec.G31;

import java.io.*;
import java.net.*;

public class UDPchannel 
{
    private UDPclient _client;
    private UDPserver _server;

    public UDPchannel(InetAddress address, int serverPort){
        try{
            _server = new UDPserver(serverPort);
            _server.start();
        }catch(IOException e){
            System.out.println("Error while creating UDP server");
            e.printStackTrace();
        }
    }

    public void sendMessage(String destAddress, int destPort, String msg){
        try{
            _client = new UDPclient(destAddress, destPort, msg);
        }catch(IOException e){
            System.out.println("Error while sending message");
            e.printStackTrace();
        }
    }

}