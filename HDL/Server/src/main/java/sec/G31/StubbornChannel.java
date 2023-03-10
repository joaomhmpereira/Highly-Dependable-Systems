package sec.G31;
import java.util.logging.Logger;

import sec.G31.messages.Message;

import java.net.*;

public class StubbornChannel
{
    private final static Logger LOGGER = Logger.getLogger(StubbornChannel.class.getName());
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
            }

            public void run(){
                //while(true){
                //    System.out.printf("SC:: %s %d %s\n", _dest, _port, _msg);
                //    _udpChannel.sendMessage(_dest, _port, _msg);
                //    try {
                //        Thread.sleep(500);
                //    } catch (InterruptedException e) {
                //        e.printStackTrace();
                //    }
                //}
                //System.out.printf("SC:: %s %d %s\n", _dest, _port, _msg);
                _udpChannel.sendMessage(_dest, _port, _msg);
            }
        }
        Thread t1 = new Thread(new StubbornSender(destAddress,destPort, msg));
        t1.start();

        //System.out.printf("SC:: %s %d %s\n", destAddress, destPort, msg);
        //_udpChannel.sendMessage(destAddress, destPort, msg);
    }

    

    public void receivedMessage(Message msg, int port, InetAddress address){
        //LOGGER.info("SC:: received message");
        _pac.receivedMessage(msg, port, address);
    }
}