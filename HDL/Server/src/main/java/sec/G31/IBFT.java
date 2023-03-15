package sec.G31;

import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.List;

import sec.G31.messages.DecidedMessage;
import sec.G31.messages.Message;
import java.util.ArrayList;


// nao sei se vai ter que ser uma thread actually 

/**
 * IBFT will be responsible for the start and control over the IBFT 
 * the server will only call the IBFT when sending a new message
 */
public class IBFT
{ 
    // for logging
    private final static Logger LOGGER = Logger.getLogger(IBFT.class.getName());

    private Server _server;
    private int _leader; // algorithm 3 and 4 whill change this
    
    // algorithm variables
    private BroadcastManager _broadcast; // broadcast 
    private int _instance; 
    private int _currentRound;
    private int _preparedRound;
    private String _preparedValue;
    private String _inputValue;
    private int _clientPort;
    //private boolean _sentPrepare;
    private boolean _sentCommit; 
    private boolean _decided;
    //private int _numCommitsReceived; // 
    private int _F; // faulty nodes
    private List<Message> _receivedMessages = new ArrayList<>(); // stores the received messages
    private Hashtable<String, ArrayList<Integer>> _prepareQuorum; // <value, list of guys that sent us prepare> 
    private Hashtable<String, ArrayList<Integer>> _commitQuorum; // <value, list of guys that sent us commit> 
    private final String PREPARE_MSG = "PREPARE";
    private final String PRE_PREPARE_MSG = "PRE-PREPARE";
    private final String COMMIT_MSG = "COMMIT";
    
    /**
        we only responde to messages of the same instance, we discard every message 
        that is from an instance from the future. Nao ha problema porque gracas ao 
        stubborn channel esssa mensagem eventualmente volta a vir para nos
     */
    

    /**
     * The constructor
     * for now the servers will know who is leader
     */
    public IBFT(Server server, int faultyNodes){
        _server = server;
        _instance = 1;
        _currentRound = 0;
        _preparedRound = 0;
        _preparedValue = "";
        _prepareQuorum = new Hashtable<String, ArrayList<Integer>>();
        _commitQuorum = new Hashtable<String, ArrayList<Integer>>();
        _broadcast = new BroadcastManager(this, server, server.getBroadcastNeighbours());
        _F = faultyNodes; 
        //_sentPrepare = false;
        _decided = false;
        _sentCommit = false;
        _leader = 1; // just for this implementation
    }

    
    public void cleanup(){
        _currentRound = 0;
        _preparedRound = 0;
        _preparedValue = "";
        _inputValue = "";
        _prepareQuorum.clear();
        _commitQuorum.clear();
        _receivedMessages.clear();
        _sentCommit = false;
        _decided = false;
    }

    /**
     * the server wants to send a new value 
     * for now there is only 1 instance and only 1 round so there is no problem
     */
    public void start(String value, int instance, int clientPort){
        //LOGGER.info("IBFT:: started");
        //System.out.println("IBFT:: started instance: " + instance + " value: " + value);
        _inputValue = value;
        _instance = instance;
        _clientPort = clientPort;

        if(_server.getId() == _leader){ // if it's the leader
            Message prePrepareMessage = new Message(PRE_PREPARE_MSG, _instance, _currentRound, _inputValue, _server.getId(), _server.getPort());
            _broadcast.sendBroadcast(prePrepareMessage);
        }
        // set timer -> ainda nao precisamos porque ainda nao ha rondas
    }


    public void sendPrepares(int instance, int round, String value){
        // set timer -> ainda nao precisamos porque ainda nao ha rondas
        Message prepareMessage;
        if (_server.isFaulty()){
            prepareMessage = new Message(PREPARE_MSG, instance, round, "DIFFERENT VALUE", 1, _server.getPort());
            
        } else {
            prepareMessage = new Message(PREPARE_MSG, instance, round, value, _server.getId(), _server.getPort());
        }
        _broadcast.sendBroadcast(prepareMessage);
    }

    public void receivedPrepareQuorum(int round, String value){
        // for future reference of IBFT
        _preparedRound = round;
        _preparedValue = value;


        Message commitMessage = new Message(COMMIT_MSG, _instance, _preparedRound, _preparedValue, _server.getId(), _server.getPort());
        _broadcast.sendBroadcast(commitMessage);  // broadcast
    }

    /**
     * when we receive a quorum of commit
     */
    public void receivedCommitQuorum(Message msg){
        // stop timer -> ainda nao precisamos porque ainda nao ha rondas
        // DECIDE -> dar append da string à blockchain
        LOGGER.info(" [SERVER " + _server.getId() + "] ===== DECIDED =====      Value -> " + msg.getValue());
        //System.out.println("===== DECIDIMOS ===== ---> " + msg.getValue());
        if (_server.getId() == _leader){
            //System.out.println("SENDING DECIDE TO CLIENT");
            DecidedMessage decideMessage = new DecidedMessage(msg.getValue(), _server.getId(), _instance);
            _broadcast.sendDecide(decideMessage, _clientPort);
        }
        _server.addToBlockchain(msg.getValue());
        _instance += 1;

        //System.out.println("IBFT:: Blockchain: " + _server.getBlockchain());
        //LOGGER.info("[SERVER " + _server.getId() + "] CLEANING UP");;
        this.cleanup();    
    }


    /**
     * when we receive a pre-prepare 
     */
    public void receivePrePrepare(Message msg){
        // verificar a autenticacao e no perfect channel

        // if it's from the leader of this round and instance
        if(msg.getInstance() == _instance && msg.getRound() == _currentRound && 
                msg.getSenderId() == _leader && !_receivedMessages.contains(msg)){
            // set timer to running
            //_sentPrepare = true;
            _receivedMessages.add(msg);
            this.sendPrepares(_instance, _currentRound, msg.getValue());
        }
    }

    /**
     * when we receive a prepare
     */
    public void receivePrepare(Message msg){
        // verificar a autenticacao no perfect channel
        if(_decided){
            return;
        }
        // if it's the same round and instance as ours
        // and has not received the message yet
        if(msg.getInstance() == _instance && msg.getRound() == _currentRound
                && !_receivedMessages.contains(msg) && !_decided){
            _receivedMessages.add(msg);
            String value = msg.getValue();
            //update the quorum or insert new entry if it isn't there
            // if still no one had sent prepare
            synchronized(this){
                if (!_prepareQuorum.containsKey(value)) {
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    list.add(msg.getSenderId());
                    _prepareQuorum.put(value, list);
                } else {
                    ArrayList<Integer> list = _prepareQuorum.get(value);
                    if(!list.contains(msg.getSenderId())){
                        list.add(msg.getSenderId());
                        _prepareQuorum.put(value, list);
                    }
                }
            }

            //System.out.println("[SERVER " + _server.getId() + "] Prepare quorum size for value: " + value  + " -> " + _prepareQuorum.get(value).size());
            
            // only send one commit if we have already quorum
            if(_prepareQuorum.get(value).size() >= 2*_F+1 && !_sentCommit && !_decided){ // in case of quorum
                _sentCommit = true;
                this.receivedPrepareQuorum(_currentRound, value);
            }
        }
    }   

    /**
     * when we receive a commit
     */
    public void receiveCommit(Message msg){
        // verificar a autenticacao no perfect channel 
        if(_decided){
            return;
        }
        // if it's the same round and instance as ours
        // and has not received the message yet
        if(msg.getInstance() == _instance && msg.getRound() == _currentRound
                && !_receivedMessages.contains(msg) && !_decided){
            _receivedMessages.add(msg);
            String value = msg.getValue();
            
            synchronized(this){
                // if still no one had sent commit
                if (!_commitQuorum.containsKey(value)) {
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    list.add(msg.getSenderId());
                    _commitQuorum.put(value, list);
                } else {
                    ArrayList<Integer> list = _commitQuorum.get(value);
                    if(!list.contains(msg.getSenderId())){
                        list.add(msg.getSenderId());
                        _commitQuorum.put(value, list);
                    }
                }
            }
            
            //System.out.println("[SERVER " + _server.getId() + "] Commit quorum size for value: " + value  + " -> " + _commitQuorum.get(value).size());

            // decide the value only one time
            if(_commitQuorum.get(value).size() >= 2*_F+1 && !_decided){
                _decided = true;
                //System.out.println("[SERVER " + _server.getId() + "] <<< received " + _commitQuorum.get(value).size() + " commits for the value >>> " + value);
                this.receivedCommitQuorum(msg); // we received a quorum
            }
        }
    }

    public int getConsensusInstance(){
        return _instance;
    }
}
