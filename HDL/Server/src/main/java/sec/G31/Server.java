package sec.G31;

import java.io.*;
import java.net.*;

public class Server
{
    private InetAddress _address;
    private int _port;
    private PerfectAuthChannel _channel;

    public Server(InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _port = serverPort;
        _channel = new PerfectAuthChannel(this, _address, _port);
    }

    public void sendMessage(String destServer, int destPort, String msg ){
        System.out.printf("SERVER:: %s %d %s\n", destServer, destPort, msg);
        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(destServer);
            _channel.sendMessage(serverAddress, destPort, msg);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void receivedMessage(String txt, int port, InetAddress address){
        System.out.println("SERVER:: received Message from my UDP server");
        _channel.sendMessage(address, port, "RESPOSTA");
    }
}