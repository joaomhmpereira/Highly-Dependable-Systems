package sec.G31.client;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import sec.G31.Account;
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
    private boolean _first; // to stop race condition, maybe
    private float _lastWeakRead;
    private float _lastStrongRead;


    public BroadcastManagerClient(InetAddress address, int port, Hashtable<Integer, Integer> servers, int clientId, int numFaulties){
        _PAChannel = new PerfectAuthClient(this, address, port, servers);
        _broadcastServers = servers;
        _clientId = clientId;
        _decidedQuorum = new Hashtable<String, ArrayList<Integer>>();
        _lastDecidedInstance = 0; // last instance that we know decided
        _decidedInstances = new ArrayList<Integer>(); 
        _F = numFaulties;
        _first = true;
    }
    

    public void printCreate(DecidedMessage msg){
        if (msg.getValue().equals("Success")) {
            System.out.println("[CLIENT " + _clientId + "] Account created sucessfully");
        } else {
            System.out.println("[CLIENT " + _clientId + "] " + msg.getValue());
        }
    }

    public void printBalance(DecidedMessage msg){
        if (msg.getBalance() != -1){
            System.out.println("[CLIENT " + _clientId + "] Balance: " + msg.getBalance());
            _lastStrongRead = msg.getBalance();
        }
        else 
            System.out.println("[CLIENT " + _clientId + "] " + msg.getValue());
    }

    public void printTransaction(DecidedMessage msg){
        if (msg.getValue().equals("Success")) {
            System.out.println("[CLIENT " + _clientId + "] Transaction sucessful");
        } else {
            System.out.println("[CLIENT " + _clientId + "] " + msg.getValue() );
        }
    }
    

    /**
     * The behaviour after receiving a decided message from a server
     * that needs a quorum of the same message
     * 
     * IMPORTANT: 
     *  we will not process every decided from an older instance
     */
    public void receivedForQuorum(DecidedMessage msg) {
        if(msg.getId() < _lastDecidedInstance) {
            return;
        }
        try{
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

            // if(!_first || _decidedQuorum.get(impStuff).size() < 2*_F+1) {
            if(_decidedQuorum.get(impStuff) != null && _decidedQuorum.get(impStuff).size() < 2*_F+1) {
                return;
            }
            // only decide if there is a quorum
            // _first = false;
            _lastDecidedInstance = msg.getId();
            _decidedInstances.add(_lastDecidedInstance);
            switch (msg.getType()) {
                case "CREATE":
                    this.printCreate(msg);
                    break;
                case "TRANSACTION":
                    this.printTransaction(msg);
                    break;
                case "S_BALANCE":
                    this.printBalance(msg);
                    break;
                default:
                    break;
            }
            _first = true;
            _decidedQuorum.remove(impStuff);
        } catch (NullPointerException e) {
            // nothing
        }
    }

    public void receivedWeakBalance(DecidedMessage msg) {
        
        if (msg.getBalance() != -1){
            Hashtable<Integer, String> signatures = msg.getSignatures();
            //Hashtable<PublicKey, Account> accounts = msg.getAccounts();
            List<Account> accounts = msg.getAccounts();
            System.out.println("[CLIENT " + _clientId + "] Total number of signatures: " + signatures.size());
            for (Integer serverId : signatures.keySet()) {
                String signature = signatures.get(serverId);
                if (!_PAChannel.verifySignature(signature, accounts.toString(), serverId)) {
                    System.out.println("[CLIENT " + _clientId + "] Signature verification failed");
                    _lastWeakRead = -2.0f;
                    return;
                }
            }

            System.out.println("[CLIENT " + _clientId + "] Balance: " + msg.getBalance());
            _lastWeakRead = msg.getBalance();
            System.out.println("[CLIENT " + _clientId + "] Signature verification successful. Servers that signed the snapshot: " + signatures.keySet().toString());
        }
        else 
            System.out.println("[CLIENT " + _clientId + "] " + msg.getValue());
    }

    public void receivedDecided(DecidedMessage msg) {
        // drop if older
        if(msg.getId() < _lastDecidedInstance 
                || _decidedInstances.contains(msg.getId())) {
            return;
        }
        
        switch (msg.getType()) {
            case "W_BALANCE":
                this.receivedWeakBalance(msg);
                break;
            case "S_BALANCE":
                this.receivedForQuorum(msg);
                break;
            case "CREATE":
                this.receivedForQuorum(msg);
                break;
            case "TRANSACTION":
                this.receivedForQuorum(msg);
                break;
            default:
                break;
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

    public void sendWeakBalanceRequest(Message msg, int serverId){
        try {
            InetAddress destAddr = InetAddress.getByName("127.0.0.1");
            _PAChannel.sendMessage(destAddr, _broadcastServers.get(serverId), msg);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public int getClientId(){
        return _clientId;
    }

    public float getLastStrongRead(){
        System.out.println("::: last strong read ::: " + _lastStrongRead);
        return _lastStrongRead;
    }

    public float getLastWeakRead(){
        System.out.println("::: last weak read ::: " + _lastWeakRead);
        return _lastWeakRead;
    }
}