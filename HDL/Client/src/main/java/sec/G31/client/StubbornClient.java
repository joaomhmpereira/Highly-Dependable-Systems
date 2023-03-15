package sec.G31.client;
import java.util.*;
//import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

import sec.G31.messages.AckMessage;
import sec.G31.messages.DecidedMessage;
import sec.G31.messages.Message;

import java.net.*;

public class StubbornClient
{
    //private final static Logger LOGGER = Logger.getLogger(StubbornChannel.class.getName());
    private UDPChannelClient _udpChannel;
    private InetAddress _address; 
    private int _port;
    private PerfectAuthClient _pac;
    private List<DecidedMessage> _receivedMessages = new ArrayList<>(); // stores the received messages
    private ConcurrentHashMap<Integer, ArrayList<Message>> _currentlySendingMessages;


    public StubbornClient(PerfectAuthClient pac, InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _port = serverPort;
        _udpChannel = new UDPChannelClient(this, _address, _port);
        _pac = pac;
        _currentlySendingMessages = new ConcurrentHashMap<Integer, ArrayList<Message>>();
    }

   
    /**
     * TO-DO: Change the msg to an object with sequence numbers and seqId's 
     *
     * @param destAddress
     * @param destPort
     * @param msg
     */
    public void sendMessage(InetAddress destAddress, int destPort, Message msg){
        class StubbornSender implements Runnable{
            InetAddress _dest;
            int _port;
            Message _msg;
            public StubbornSender(InetAddress destAddress, int destPort, Message msg){
                _dest = destAddress;
                _port = destPort;
                _msg = msg;
                if(_currentlySendingMessages.containsKey(destPort)){
                    _currentlySendingMessages.get(destPort).add(msg);
                }else{
                    ArrayList<Message> newList = new ArrayList<Message>();
                    newList.add(msg);
                    _currentlySendingMessages.put(destPort, newList);
                }
            }

            public void run(){
                while(_currentlySendingMessages.get(_port).contains(_msg)){
                    //System.out.printf("SC:: %s %d %s\n", _dest, _port, _msg);
                    _udpChannel.sendMessage(_dest, _port, _msg);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ////System.out.printf("SC:: %s %d %s\n", _dest, _port, _msg);
                //_udpChannel.sendMessage(_dest, _port, _msg);
            }
        }
        Thread t1 = new Thread(new StubbornSender(destAddress,destPort, msg));
        t1.start();

        //System.out.printf("SC:: %s %d %s\n", destAddress, destPort, msg);
        //_udpChannel.sendMessage(destAddress, destPort, msg);
    }

    

    public void receivedMessage(DecidedMessage msg, int port, InetAddress address){
        //LOGGER.info("SC:: received message");
        //System.out.println("SC:: received message");
        if (_receivedMessages.contains(msg)){
            //System.out.println("SC:: received message already");
            return;
        }
        _receivedMessages.add(msg);
        _pac.receivedMessage(msg, port, address);
    }

    /**
     * when we received a Ack Message for a normal message
     */
    public void receivedAck(AckMessage msg, int port){
        Message am = msg.getAckedMessage();
        ArrayList<Message> portMessages = _currentlySendingMessages.get(port);
        for (int i = 0; i < portMessages.size(); i++){
            if (am.equals(portMessages.get(i))){
                _currentlySendingMessages.get(port).remove(i);
                break;
            }
        }
    }
}