package sec.G31;
import java.util.logging.Logger;

import sec.G31.messages.Message;


// nao sei se vai ter que ser uma thread actually 

/**
 * IBFT will be responsible for the start and control over the IBFT 
 * the server will only call the IBFT when sending a new message
 */
public class IBFT
{ 
    private Server _server;

    // algorithm variables
    private int _instance; 
    private int _currentRound;
    private int _preparedRound;
    private String _preparedValue;
    private String _inputValue;
    //private DBManager managaer

    // manager.newPrePrepareBroadcast(msg)

    // private Timer _timer;


    /**
     * The constructor
     */
    public IBFT(Server server){
        _server = server;
        _instance = 0;
        _currentRound = 0;
        _preparedRound = 0;
        _preparedValue = "";
    }

    /**
     * the server wants to send a new value 
     */
    public void start(String value, int instance){
        _inputValue = value;
        _instance = instance;
        //_currentRound = 1;
        //_preparedRound = null;
        //_preparedValue = null;

        int sequenceNumber = 1;

        
        if(_server.isLeader(_currentRound)){
            Message prePreparMessage = new Message("PRE-PREPARE", _instance, sequenceNumber, _currentRound, _inputValue);
            //manager.broadcastPrePrepare(prePreparMessage);
        }
        // set timer -> ainda nao precisamos porque ainda nao ha rondas
    }


    public void sendPrepares(){
        // set timer -> ainda nao precisamos porque ainda nao ha rondas
        int sequenceNumber = 1;
        Message prepareMessage = new Message("PREPARE", _instance, sequenceNumber, _preparedRound, _inputValue);
        //manager.broadcastPrepare(prepareMessage);
    }

    public void sendCommits(){
        _preparedRound = _currentRound;
        _preparedValue = _inputValue;

        int sequenceNumber = 1;

        Message commitMessage = new Message("COMMIT", _instance, sequenceNumber, _preparedRound, _preparedValue);
        //manager.broadcastCommit(commitMessage);
    }

    public void receivedCommitQuorum(){
        // stop timer -> ainda nao precisamos porque ainda nao ha rondas
        // DECIDE -> dar append da string à blockchain
    }


}
