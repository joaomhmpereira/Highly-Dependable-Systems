package sec.G31;

import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.List;

import sec.G31.messages.DecidedMessage;
import sec.G31.messages.Message;
import sec.G31.messages.TransactionMessage;
import sec.G31.utils.TransactionBlock;

import java.security.PublicKey;
import java.util.ArrayList;



/**
 * IBFT will be responsible for the start and control over the IBFT 
 * the server will only call the IBFT when sending a new message
 */
public class IBFT
{ 
    // for logging
    private final static Logger LOGGER = Logger.getLogger(IBFT.class.getName());

    private Server _server;
    private int _leader; // algorithm 3 and 4 will change this
    
    // algorithm variables
    private BroadcastManager _broadcast; // broadcast 
    private int _instance; 
    private int _currentRound;
    private int _preparedRound;
    private TransactionBlock _preparedValue;
    private TransactionBlock _inputValue;
    private int _clientPort;
    //private boolean _sentPrepare;
    private boolean _sentCommit; 
    private boolean _decided;
    //private int _numCommitsReceived; // 
    private int _F; // faulty nodes
    private int _nonceCounter;
    private List<Message> _receivedMessages = new ArrayList<>(); // stores the received messages
    private Hashtable<TransactionBlock, ArrayList<Integer>> _prepareQuorum; // <value, list of ports that sent us prepare> 
    private Hashtable<TransactionBlock, ArrayList<Integer>> _commitQuorum; // <value, list of ports that sent us commit> 
    private final String PREPARE_MSG = "PREPARE";
    private final String PRE_PREPARE_MSG = "PRE-PREPARE";
    private final String COMMIT_MSG = "COMMIT";

    private TransactionBlock _currentTransactionBlock;
    private Hashtable<PublicKey, Account> _accounts;
    
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
        //_preparedValue = "";
        _prepareQuorum = new Hashtable<TransactionBlock, ArrayList<Integer>>();
        _commitQuorum = new Hashtable<TransactionBlock, ArrayList<Integer>>();
        _broadcast = new BroadcastManager(this, server, server.getBroadcastNeighbours());
        _F = faultyNodes; 
        //_sentPrepare = false;
        _decided = false;
        _sentCommit = false;
        _leader = 1; // just for this implementation
        _currentTransactionBlock = new TransactionBlock();
        _accounts = new Hashtable<PublicKey, Account>();
        _nonceCounter = 0;
    }

    /**
     * Create a new account (if it doesn't already exist) and add it to the list of accounts
     * @param publicKey
     */
    public void createAccount(PublicKey publicKey, int clientPort){
        if (!_accounts.containsKey(publicKey)){
            // initial balance fixed value > 0
            Account newAccount = new Account(publicKey, 150);
            _accounts.put(publicKey, newAccount);
            System.out.println("[SERVER " + _server.getId() + "]: Account created successfully. Client: " + clientPort);
            if (_server.isLeader(_currentRound)){
                DecidedMessage decidedMessage = new DecidedMessage("CREATE", "Success", _server.getId(), -1, _nonceCounter);
                _broadcast.sendDecide(decidedMessage, clientPort);
                _nonceCounter++;
            } // TODO sending only if we're leader
        } else {
            System.out.println("Account already exists");
            if (_server.isLeader(_currentRound)){
                DecidedMessage decidedMessage = new DecidedMessage("CREATE", "Error: Account already exists.", _server.getId(), -1, _nonceCounter);
                _broadcast.sendDecide(decidedMessage, clientPort);
                _nonceCounter++;
            }   
        }
    }

    public void checkBalance(PublicKey publicKey, int clientPort){
        if (_accounts.containsKey(publicKey)){
            synchronized (this){    // necessario??
                Account account = _accounts.get(publicKey);
                System.out.println("[SERVER " + _server.getId() + "]: Balance checked successfully. Client: " + clientPort);
                if (_server.isLeader(_currentRound)){
                    DecidedMessage decidedMessage = new DecidedMessage("BALANCE", account.getBalance(), _server.getId(), _nonceCounter);
                    _broadcast.sendDecide(decidedMessage, clientPort);
                    _nonceCounter++;
                } //TODO sending only if we're leader
            }
        } else {
            if (_server.isLeader(_currentRound)) {
                DecidedMessage decidedMessage = new DecidedMessage("BALANCE", -1, _server.getId(), _nonceCounter);
                _broadcast.sendDecide(decidedMessage, clientPort);
                _nonceCounter++;
            }
            System.out.println("Account doesn't exist");
        }
    }

    public void makeTransaction(TransactionMessage msg, int clientPort){
        if (_accounts.containsKey(msg.getSource()) && _accounts.containsKey(msg.getDestination())){
            synchronized (this) {
                Account source = _accounts.get(msg.getSource());
                Account destination = _accounts.get(msg.getDestination());
                if (source.subtractBalance(msg.getAmount())){
                    destination.addBalance(msg.getAmount());
                    if (_server.isLeader(_currentRound)){
                        DecidedMessage decidedMessage = new DecidedMessage("TRANSACTION", "Success", _server.getId(), -1, _nonceCounter);
                        _broadcast.sendDecide(decidedMessage, clientPort);
                        _nonceCounter++;
                    }
                    _currentTransactionBlock.addTransaction(msg);
                    if (_currentTransactionBlock.isCompleted()){
                        this.start(_currentTransactionBlock, clientPort, clientPort);
                    }
                } else {
                    System.out.println("Not enough balance");
                    if (_server.isLeader(_currentRound)){
                        DecidedMessage decidedMessage = new DecidedMessage("TRANSACTION", "Error: Not enough balance.", _server.getId(), -1, _nonceCounter);
                        _broadcast.sendDecide(decidedMessage, clientPort);
                        _nonceCounter++;
                    }
                }
            }
            
        } else {
            System.out.println("one of the accounts doesn't exist");
            if (_server.isLeader(_currentRound)){
                DecidedMessage decidedMessage = new DecidedMessage("TRANSACTION", "Error: One of the accounts doesn't exist.", _server.getId(), -1, _nonceCounter);
                _broadcast.sendDecide(decidedMessage, clientPort);
                _nonceCounter++;
            }
        }
    }
    
    public void cleanup(){
        _currentRound = 0;
        _preparedRound = 0;
        //_preparedValue = "";
        //_inputValue = "";
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
    public void start(TransactionBlock value, int instance, int clientPort){
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


    public void sendPrepares(int instance, int round, TransactionBlock value){
        // set timer -> ainda nao precisamos porque ainda nao ha rondas
        Message prepareMessage;
        if (_server.isFaulty()){
            TransactionMessage byzantineMessage = new TransactionMessage(null, null, 20);
            TransactionBlock byzantineBlock = new TransactionBlock();
            byzantineBlock.addTransaction(byzantineMessage);
            prepareMessage = new Message(PREPARE_MSG, instance, round, byzantineBlock, 1, _server.getPort());
            
        } else {
            prepareMessage = new Message(PREPARE_MSG, instance, round, value, _server.getId(), _server.getPort());
        }
        _broadcast.sendBroadcast(prepareMessage);
    }

    public void receivedPrepareQuorum(int round, TransactionBlock value){
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
        // DECIDE -> dar append da string à blockchain
        LOGGER.info(" [SERVER " + _server.getId() + "] ===== DECIDED =====      Value -> " + msg.getBlock());

        // send decided message to client
        //DecidedMessage decideMessage = new DecidedMessage("decided", _server.getId(), _instance, _nonceCounter);
        //_broadcast.sendDecide(decideMessage, _clientPort);
        //_nonceCounter++;
        //if (_currentTransactionBlock.isCompleted()){ // if the block is completed -> add it to the blockchain and create new one
        //    _server.addToBlockchain(_currentTransactionBlock);
        //    _currentTransactionBlock = new TransactionBlock();
        //    _currentTransactionBlock.addTransaction(msg.getValue());
        //} else {
        //    _currentTransactionBlock.addTransaction(msg.getValue());
        //}
        //_server.addToBlockchain(msg.getValue());
        _instance += 1;

        this.cleanup();
        //synchronized(_broadcast){
        //    _broadcast.notifyAll();
        //}
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
            System.out.println(" [SERVER " + _server.getId() + "] received pre-prepare from leader");
            System.out.println(_inputValue.equals(msg.getBlock()));
            this.sendPrepares(_instance, _currentRound, msg.getBlock());
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
            TransactionBlock value = msg.getBlock();
            //update the quorum or insert new entry if it isn't there
            // if still no one had sent prepare

            synchronized(this){
                for (TransactionBlock block : _prepareQuorum.keySet()){
                    if (value.equals(block)){
                        System.out.println("Codes, 1: " + value.hashCode() + " 2 (in the quorum): " + block.hashCode());
                        System.out.println("FOUND ONE");
                    }
                }
                if (!_prepareQuorum.containsKey(value)) {
                    System.out.println("Doesn't contain key, adding " + value.hashCode());
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

            System.out.println("[SERVER " + _server.getId() + "] Prepare quorum size for value: " + value.hashCode()  + " -> " + _prepareQuorum.get(value).size());
            
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
            TransactionBlock value = msg.getBlock();
            
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
                System.out.println("[SERVER " + _server.getId() + "] <<< received " + _commitQuorum.get(value).size() + " commits for the value >>> " + value);
                this.receivedCommitQuorum(msg); // we received a quorum
            }
        }
    }

    public int getConsensusInstance(){
        return _instance;
    }
}
