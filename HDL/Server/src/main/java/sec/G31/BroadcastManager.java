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

public class BroadcastManager
{
    // attributes that are known to everyother guy 
    private PerfectAuthChannel _channel; // the channel that it uses for communication
    private Hashtable<Integer, Integer> _broadcastNeighbors; // to send broadcast
    private IBFT _ibft;

    public BroadcastManager(IBFT ibft, Server server, Hashtable<Integer, Integer> neighbours){
        _ibft = ibft;
        _broadcastNeighbors = neighbours;
        _channel = new PerfectAuthChannel(this, server, server.getAddress(), server.getPort(), _broadcastNeighbors);
    } 

    /**
     * when it was received a message we must know 
     * to whom it may concern
     * we need to know the object type and if there is an 
     * object of that type in our database
     */
    public void receivedMessage(Message msg){
        //System.out.println(msg.toString());
        String type = msg.getType();
        switch(type){
            case "PRE-PREPARE":
                System.out.println("received pre-prepare: " + msg.toString());
                _ibft.receivePrePrepare(msg);
                break;
            case "PREPARE":
                System.out.println("received prepare: " + msg.toString());
                _ibft.receivePrepare(msg);
                break;
            case "COMMIT":
                System.out.println("received commit: " + msg.toString());
                _ibft.receiveCommit(msg);
                break;
        }
    }

    /**
     * sends a message to every server
     */
    public void sendBroadcast(Message msg){
        try {
            InetAddress destAddr = InetAddress.getByName("127.0.0.1");
            for(int i = 1; i <= _broadcastNeighbors.size(); i++){ // send to all servers 
                _channel.sendMessage(destAddr, _broadcastNeighbors.get(i), msg);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } // just localhost
    }
}