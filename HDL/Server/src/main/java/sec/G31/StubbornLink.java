package sec.G31;
import java.util.*;
// import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

import sec.G31.messages.*;

import java.net.*;

public class StubbornLink
{
    // private final static Logger LOGGER = Logger.getLogger(StubbornChannel.class.getName());
    private UDPchannel _UDPchannel;
    private InetAddress _address; 
    private int _port;
    private PerfectAuthLink _PACchannel;
    private List<Message> _receivedMessages;
    // <port, list<messages>> if a message is still in this list, the port hasn't acked the message 
    private ConcurrentHashMap<Integer, ArrayList<Message>> _currentlySendingMessages;
                            // = new Hashtable<Integer, ArrayList<Message>>();
    
    // list of decided messages that we are currently sending for each port 
    private ConcurrentHashMap<Integer, ArrayList<DecidedMessage>> _currentlySendingDecidedMessages;
                                    // = new Hashtable<Integer, ArrayList<DecidedMessage>>()

    /**
     * this will be part of a perfect auth channel
     */
    public StubbornLink(PerfectAuthLink pac, InetAddress serverAddress, int serverPort){
        _address = serverAddress;
        _port = serverPort;
        _UDPchannel = new UDPchannel(this, _address, _port);
        _PACchannel = pac;
        _receivedMessages = new ArrayList<Message>();
        _currentlySendingDecidedMessages = new ConcurrentHashMap<Integer, ArrayList<DecidedMessage>>();
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
            
            /**
             * this is vulnerable to a DOS attack, too much memory :(
             */
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
                // if this messages is still in the list of acked messages for that port 
                while(_currentlySendingMessages.get(_port).contains(_msg)){
                    //System.out.printf("SC:: %s %d %s\n", _dest, _port, _msg);
                    _UDPchannel.sendMessage(_dest, _port, _msg);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //System.out.printf("SC:: %s %d %s\n", _dest, _port, _msg);
                //_UDPchannel.sendMessage(_dest, _port, _msg);
            }
        }
        Thread t1 = new Thread(new StubbornSender(destAddress,destPort, msg));
        t1.start();

        //System.out.printf("SC:: %s %d %s\n", destAddress, destPort, msg);
        //_UDPchannel.sendMessage(destAddress, destPort, msg);
    }

    /**
     * TO-DO: Change the msg to an object with sequence numbers and seqId's 
     *
     * @param destAddress
     * @param destPort
     * @param msg
     */
    public void sendDecide(InetAddress destAddress, int destPort, DecidedMessage msg){
        class StubbornSender implements Runnable{
            InetAddress _dest;
            int _port;
            DecidedMessage _msg;
            public StubbornSender(InetAddress destAddress, int destPort, DecidedMessage msg){
                _dest = destAddress;
                _port = destPort;
                _msg = msg;
                if(_currentlySendingDecidedMessages.containsKey(destPort)){
                    _currentlySendingDecidedMessages.get(destPort).add(msg);
                }else{
                    ArrayList<DecidedMessage> newList = new ArrayList<DecidedMessage>();
                    newList.add(msg);
                    _currentlySendingDecidedMessages.put(destPort, newList);
                }
                //_currentlySendingDecidedMessages.put(destPort, _msg);
            }

            public void run(){
                int timeout = 100;
                //System.out.println("SC:: sending decided message");
                while(_currentlySendingDecidedMessages.get(_port).contains(_msg)){
                    //System.out.printf("SC:: %s %d %s\n", _dest, _port, _msg);
                    _UDPchannel.sendDecide(_dest, _port, _msg);
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeout *= 2;
                }
                //System.out.printf("SC:: %s %d %s\n", _dest, _port, _msg);
                //_UDPchannel.sendDecide(_dest, _port, _msg);
            }
        }
        Thread t1 = new Thread(new StubbornSender(destAddress,destPort, msg));
        t1.start();

        //System.out.printf("SC:: %s %d %s\n", destAddress, destPort, msg);
        //_UDPchannel.sendMessage(destAddress, destPort, msg);
    }

    public void receivedMessage(Message msg, int port, InetAddress address){
        if (_receivedMessages.contains(msg)){
            System.out.println("... received message already ... " + msg);
            return;
        }
        System.out.println("!!! received new message !!! " + msg);
        _receivedMessages.add(msg);
        _PACchannel.receivedMessage(msg, port, address);
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

    /**
     * when we receive a ack message for a decide message
     */
    public void receivedAckDecided(AckMessage msg, int port){
        DecidedMessage dm = msg.getAckedDecidedMessage();
        ArrayList<DecidedMessage> portMessages = _currentlySendingDecidedMessages.get(port);
        for (int i = 0; i < portMessages.size(); i++){
            if (dm.equals(portMessages.get(i))){
                _currentlySendingDecidedMessages.get(port).remove(i);
                break;
            }
        }
    }
}