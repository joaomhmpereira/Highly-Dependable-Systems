package sec.G31;
import java.net.*;
import java.util.Hashtable;
import sec.G31.messages.Message;

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
    private Hashtable<Message, DoubleEchoBroadcast> _prePrepHashtable;
    private Hashtable<Message, DoubleEchoBroadcast> _prepareHashtable;
    private Hashtable<Message, DoubleEchoBroadcast> _commitHashtable;
    private int _leader; 
    private int _F; 
    private int _numNeighbours;
    private int _N;
    private Server _server;
    private IBFT _ibft;
    // private 

    public BroadcastManager(IBFT ibft, PerfectAuthChannel channel, Server server, Hashtable<Integer, Integer> neighbours,
                    int leader, int faulty, int numberNodes, int numberNeighbours){
        _ibft = ibft;
        _channel = channel;
        _server = server;
        _broadcastNeighbors = neighbours;
        _F = faulty;
        _N = numberNodes;
        _numNeighbours = numberNeighbours;

        _channel.setBroadcastManager(this);
    } 

    /**
     * when it was received a message we must know 
     * to whom it may concern
     * we need to know the object type and if there is an 
     * object of that type in our database
     */
    public void receivedMessage(Message msg){
        /**
            String type = message.getType();
            switch(type){
                case "pre-prepare":
                    if(sub_type == "send"){
                        if(ja houver um double echo broadcast para esse){
                            broadcast.receviedSendMessage(msg);
                        }
                        else{
                            create new broadcast 
                        }
                    } else if(){
                        
                    }
                case "prepare":
                    if(sub_type == "send"){
                        // se nao houver ainda para esse criar um novo double echo broadcast
                    }
                case "commit":
                    if(sub_type == "send"){
                        // se nao houver ainda para esse criar um novo double echo broadcast
                    }
            }
        
        */
    }


}