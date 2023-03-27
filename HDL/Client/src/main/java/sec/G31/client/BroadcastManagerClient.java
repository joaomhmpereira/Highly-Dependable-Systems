package sec.G31.client;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import sec.G31.messages.*;

// nao sei se vai ter que ser uma thread actually 

/**
 * THe problems related to duplicate messages are solved in lower layers
 */

public class BroadcastManagerClient
{
    // attributes that are known to everyother guy 
    private Hashtable<Integer, Integer> _broadcastServers;
    private PerfectAuthClient _PAChannel;
    private int _clientId;
    private Hashtable<String, ArrayList<Integer>> _decidedQuorum; // <value, list of guys that sent us decided>
    private int _lastDecidedInstance;


    public BroadcastManagerClient(InetAddress address, int port, Hashtable<Integer, Integer> servers, int clientId){
        _PAChannel = new PerfectAuthClient(this, address, port, servers);
        _broadcastServers = servers;
        _clientId = clientId;
        _decidedQuorum = new Hashtable<String, ArrayList<Integer>>();
        _lastDecidedInstance = 1; // last instance that we know decided 
    }
    
    public void receivedMessage(DecidedMessage msg){
        System.out.println("[CLIENT " + _clientId + "] Value decided for instance " + msg.getInstance() + " -> " + msg.getValue());
    }

    /**
     * The behaviour after receiving a decided message from a server
     * 
     * IMPORTANT: 
     *  we will not process every decided from an older instance 
     */
    public void receiveDecided(DecidedMessage msg){
        // drop if older
        if(msg.getInstance() < _lastDecidedInstance){
            return;
        }
        // we haven't decided and we don't have this msg 
        if(msg.getInstance() >= _lastDecidedInstance){
            String value = msg.getValue();
            //update the quorum or insert new entry if it isn't there
            // if still no one had sent prepare
            synchronized(this){
                if (!_decidedQuorum.containsKey(value)) {
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    list.add(msg.getSenderId());
                    _decidedQuorum.put(value, list);
                } else {
                    ArrayList<Integer> list = _decidedQuorum.get(value);
                    if(!list.contains(msg.getSenderId())){
                        list.add(msg.getSenderId());
                        _decidedQuorum.put(value, list);
                    }
                }
            }

            //System.out.println("[SERVER " + _server.getId() + "] Prepare quorum size for value: " + value  + " -> " + _prepareQuorum.get(value).size());
            
            // only decide if there is a quorum
            // TO-DO: what will be the size of the quorum
            if(_decidedQuorum.get(value).size() >= 3){ 
                _lastDecidedInstance = msg.getInstance(); 
                this.receivedMessage(msg); 
            }
        }
    } 

    /**
     * sends a message to every server
     */
    public void sendBroadcast(Message msg){
        try {
            InetAddress destAddr = InetAddress.getByName("127.0.0.1");
            for(int i = 1; i <= _broadcastServers.size(); i++){ // send to all servers 
                //System.out.println("sending to server on port: " + _broadcastServers.get(i));
                _PAChannel.sendMessage(destAddr, _broadcastServers.get(i), msg);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public int getClientId(){
        return _clientId;
    }
}