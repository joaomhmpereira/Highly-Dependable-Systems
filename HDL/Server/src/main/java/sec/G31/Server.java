package sec.G31;
import java.util.logging.Logger;

import sec.G31.utils.TransactionBlock;

import java.net.*;
import java.util.Hashtable;

import java.security.PublicKey;

public class Server
{   
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());
    private int _id;
    private InetAddress _address;
    private String _faultType;
    private String _leaderFlag;
    private int _port;
    private IBFT _ibft;
    private Blockchain _blockchain;
    private Hashtable<Integer, Integer> _myNeighbors = new Hashtable<Integer, Integer>(); // <server id, port>

    public Server(int serverId,InetAddress serverAddress, int serverPort,
                 String faultType, String leaderFlag, int numFaulty, PublicKey leaderPubKey){
        _id = serverId;
        _address = serverAddress;
        _port = serverPort;
        //_channel = new PerfectAuthChannel(this, _address, _port);
        _ibft = new IBFT(this, numFaulty, leaderPubKey);
        _faultType = faultType;
        _leaderFlag = leaderFlag;
        _blockchain = new Blockchain();
        System.out.println("===Server " + _id + " created===");
    }

    //public void startIBFT(String value){
    //    _ibft.start(value, _instance);
    //}

    public IBFT getIBFT(){
        return _ibft;
    }

    public InetAddress getAddress(){
        return _address;
    }

    public int getPort(){
        return _port;
    }

    public int getId() {
        return _id;
    }

    public Hashtable<Integer, Integer> getBroadcastNeighbours(){
        return _myNeighbors;
    }

    public void newNeighbor(int neighborId, int neighborPort) {
        _myNeighbors.put(neighborId, neighborPort); // ver se da int para a hashtable
    }
    
    public boolean isLeader(int currentRound) {
        return _leaderFlag.equals("Y");
    }

    public boolean isFaulty() {
        return _faultType.equals("F"); 
    }

    public void addToBlockchain(TransactionBlock block){
        _blockchain.addTransactionBlock(block);
    }

    public TransactionBlock getLastDecidedValue(){
        return _blockchain.getLastDecidedValue();
    }

    public Blockchain getBlockchain(){
        return _blockchain;
    }

    public String getBlockchainString(){
        return _blockchain.toString();
    }

    public int getConsensusInstance(){
        return _blockchain.getConsensusInstance();
    }

    public void receivedMessage(String txt, int port, InetAddress address){
        LOGGER.info("SERVER:: received Message from my UDP server");
        //_channel.sendMessage(address, port, "RESPOSTA");
    }

    @Override
    public String toString(){
        return "Server Id: " + _id + " Addr:" + _address + " Port: " + _port + " Fault Type: " + _faultType + " Leader flag: " + _leaderFlag;
    }
}