package sec.G31;

import java.util.logging.Logger;
import java.util.Hashtable;
import sec.G31.messages.Message;


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
    private int _numPreparesReceived;
    private int _numCommitsReceived;
    private int _F; // faulty nodes
    private Hashtable<String, Integer> _prepareQuorum; // <value, count> of prepares received for a value for this round
    private Hashtable<String, Integer> _commitQuorum; // <value, count> of commits received for a value for this round
    

    // manager.newPrePrepareBroadcast(msg)

    // private Timer _timer;

    /**
     * The constructor
     * for now the servers will know who is leader
     */
    public IBFT(Server server, int faultyNodes){
        _server = server;
        _instance = 0;
        _currentRound = 0;
        _preparedRound = 0;
        _preparedValue = "";
        _numPreparesReceived = 0;
        _numCommitsReceived = 0;
        _broadcast = new BroadcastManager(this, server, server.getBroadcastNeighbours());
        _F = faultyNodes; 
        _leader = 1; // just for this implementation 
    }

    /**
     * the server wants to send a new value 
     * for now there is only 1 instance and only 1 round so there is no problem
     */
    public void start(String value, int instance){
        LOGGER.info("IBFT:: started");

        _inputValue = value;
        _instance = instance;
        //_currentRound = 1;
        //_preparedRound = null;
        //_preparedValue = null;

        // ze/joazoca se for faulty e mudar o server id para 1 é possivel?

        if(_server.getId() == _leader){  // if it's the leader
            Message prePrepareMessage = new Message("PRE-PREPARE", _instance, _currentRound, _inputValue, _server.getId());
            _broadcast.sendBroadcast(prePrepareMessage);
        }
        // set timer -> ainda nao precisamos porque ainda nao ha rondas
    }


    public void sendPrepares(int instance, int round, String value){
        // set timer -> ainda nao precisamos porque ainda nao ha rondas
        Message prepareMessage = new Message("PREPARE", instance, round, value, _server.getId());
        _broadcast.sendBroadcast(prepareMessage);
    }

    public void receivedPrepareQuorum(int round, String value){
        // for future reference of IBFT
        _preparedRound = round;
        _preparedValue = value;


        Message commitMessage = new Message("COMMIT", _instance, _preparedRound, _preparedValue, _server.getId());
        _broadcast.sendBroadcast(commitMessage);  // broadcast
    }

    /**
     * when we receive a quorum of commit
     */
    public void receivedCommitQuorum(){
        // stop timer -> ainda nao precisamos porque ainda nao ha rondas
        // DECIDE -> dar append da string à blockchain
    }


    /**
     * when we receive a pre-prepare 
     */
    public void receivePrePrepare(Message msg){
        // verificar a autenticacao e no perfect channel

        // if if it's from the leader of this round and instance
        if(msg.getInstance() == _instance && msg.getRound() == _currentRound && 
                msg.getSenderId() == _leader){
            // set timer to running
            this.sendPrepares(_instance, _currentRound, msg.getValue());
        }
    }

    /**
     * when we receive a prepare
     */
    public void receivePrepare(Message msg){
        // verificar a autenticacao no perfect channel
        
        // if it's the same round and instance as ours
        if(msg.getInstance() == _instance && msg.getRound() == _currentRound){
            String value = msg.getValue();
            //update the quorum or insert new entry if it isn't there
            _prepareQuorum.put(value, _prepareQuorum.getOrDefault(value, 0)+1);
            
            if(_prepareQuorum.get(value) == 2*_F+1){ // in case of quorum
                this.receivedPrepareQuorum(_currentRound, value);
            }
        }
    }   

    /**
     * when we receive a commit
     */
    public void receiveCommit(Message msg){
        // verificar a autenticacao no perfect channel 

        if(msg.getInstance() == _instance && msg.getRound() == _currentRound){
            String value = msg.getValue();

            //update the quorum or insert new entry if it isn't there
            _commitQuorum.put(value, _commitQuorum.getOrDefault(value, 0)+1);
            //if(_commitQuorum.contains(value))
            //    _commitQuorum.put(value, _prepareQuorum.get(value)+1);
            //else   
            //    _commitQuorum.put(value, 1);

            if(_commitQuorum.get(value) == 2*_F+1){
                this.receivedCommitQuorum(); // we received a quorum
            }
        }
    }
}
