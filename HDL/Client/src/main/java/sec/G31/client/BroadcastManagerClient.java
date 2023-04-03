package sec.G31.client;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
    private List<Integer> _decidedInstances;
    private int _F;
    private boolean _first = true; // to stop race condition, maybe


    public BroadcastManagerClient(InetAddress address, int port, Hashtable<Integer, Integer> servers, int clientId, int numFaulties){
        _PAChannel = new PerfectAuthClient(this, address, port, servers);
        _broadcastServers = servers;
        _clientId = clientId;
        _decidedQuorum = new Hashtable<String, ArrayList<Integer>>();
        _lastDecidedInstance = 0; // last instance that we know decided
        _decidedInstances = new ArrayList<Integer>(); 
        _F = numFaulties;
    }
    
    /*public void receivedDecidedTransfer(DecidedMessage msg){
        if (msg.getValue().equals("Success")) {
            System.out.println("[CLIENT " + _clientId + "] Sucessfull Transfer");
        } else {
            System.out.println("[CLIENT " + _clientId + "] Tranfer failed");
        }    
    }*/

    public void receivedDecidedCreate(DecidedMessage msg){
        if (msg.getValue().equals("Success")) {
            System.out.println("[CLIENT " + _clientId + "] Account created sucessfully");
        } else {
            System.out.println("[CLIENT " + _clientId + "] Account creation failed");
        }
    }

    public void receivedDecidedBalance(DecidedMessage msg){
        if (msg.getBalance() != -1)
            System.out.println("[CLIENT " + _clientId + "] Balance: " + msg.getBalance());
        else 
            System.out.println("[CLIENT " + _clientId + "] Balance request failed");
    }

    public void receivedDecidedTransaction(DecidedMessage msg){
        if (msg.getValue().equals("Success")) {
            System.out.println("[CLIENT " + _clientId + "] Transaction sucessful");
        } else {
            System.out.println("[CLIENT " + _clientId + "] " + msg.getValue() );
        }
    }
    

    /**
     * The behaviour after receiving a decided message from a server
     * 
     * IMPORTANT: 
     *  we will not process every decided from an older instance 
     */
    public void receivedDecided(DecidedMessage msg){
        //BATOTA
        // msg.setInstance(_lastDecidedInstance + 1);

        System.out.println("Decided sent to client " + _clientId);

        // drop if older
        if(msg.getId() < _lastDecidedInstance 
                || _decidedInstances.contains(msg.getId())){
            System.out.println("Before - msg: " + msg.getId() + " last: " + _lastDecidedInstance);
            return;
        }

        System.out.println("After - msg: " + msg.getId() + " last: " + _lastDecidedInstance);
        // we haven't decided and we don't have this msg 
        if(msg.getId() >= _lastDecidedInstance){
            String impStuff = msg.toString();
            //update the quorum or insert new entry if it isn't there
            // if still no one had sent prepare
            synchronized(this){
                if (!_decidedQuorum.containsKey(impStuff)) {
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    list.add(msg.getSenderId());
                    _decidedQuorum.put(impStuff, list);
                } else {
                    ArrayList<Integer> list = _decidedQuorum.get(impStuff);
                    if(!list.contains(msg.getSenderId())){
                        list.add(msg.getSenderId());
                        _decidedQuorum.put(impStuff, list);
                    }
                }
            }

            System.out.println("first: " + _first);
            // only decide if there is a quorum
            if(_first && _decidedQuorum.get(impStuff).size() >= 2*_F+1){
                //System.out.println("[CLIENT " + _clientId + "] Received Decided Quorum");
                _first = false;
                _lastDecidedInstance = msg.getId();
                _decidedInstances.add(_lastDecidedInstance);
                
                switch (msg.getType()) {
                    case "BALANCE":
                        this.receivedDecidedBalance(msg);
                        break;
                    case "CREATE":
                        this.receivedDecidedCreate(msg);
                        break;
                    case "TRANSACTION":
                        this.receivedDecidedTransaction(msg);
                        break;
                    default:
                        break;
                }
                _first = true;
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