package sec.G31;
import java.net.*;
import java.util.Hashtable;
import sec.G31.messages.*;

// nao sei se vai ter que ser uma thread actually 

/**
 * TO-DO:
 *  + associate the doubleEchoBroadcast to an actual message
 *  + Don't use strings to send the message i think, or use them very strictly
 */

public class BroadcastManagerClient
{
    // attributes that are known to everyother guy 
    private Hashtable<Integer, Integer> _broadcastServers;
    private PerfectAuthClient _channel;

    public BroadcastManagerClient(Hashtable<Integer, Integer> servers){
        _broadcastServers = servers;
    }

    public void receivedMessage(DecidedMessage msg){
        //System.out.println(msg.toString());
        //String type = msg.getType();
        System.out.println("received decided message: " + msg.toString());
    }

    /**
     * sends a message to every server
     */
    public void sendBroadcast(InitInstance msg){
        try {
            InetAddress destAddr = InetAddress.getByName("127.0.0.1");
            for(int i = 1; i <= _broadcastServers.size(); i++){ // send to all servers 
                _channel.sendMessage(destAddr, _broadcastServers.get(i), msg);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}