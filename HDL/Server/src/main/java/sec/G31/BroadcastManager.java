package sec.G31;

import java.net.*;
import java.util.Hashtable;

import sec.G31.messages.*;
import sec.G31.utils.TransactionBlock;

// nao sei se vai ter que ser uma thread actually 

/**
 * TO-DO:
 * + associate the doubleEchoBroadcast to an actual message
 * + Don't use strings to send the message i think, or use them very strictly
 */

public class BroadcastManager {
    // attributes that are known to everyother guy
    private PerfectAuthLink _PAChannel; // the channel that it uses for communication
    private Hashtable<Integer, Integer> _broadcastNeighbors; // to send broadcast
    private IBFT _ibft;
    private int _operationId;

    public BroadcastManager(IBFT ibft, Server server, Hashtable<Integer, Integer> neighbours) {
        _ibft = ibft;
        _broadcastNeighbors = neighbours;
        _PAChannel = new PerfectAuthLink(this, server, server.getAddress(), server.getPort(), _broadcastNeighbors);
        _operationId = 0;
    }

    public void receivedMessage(Message msg, int clientPort) {
        // System.out.println(msg.toString());
        String type = msg.getType();
        switch (type) {
            case "PRE-PREPARE":
                //System.out.println("received pre-prepare: " + msg.toString());
                _ibft.receivePrePrepare(msg);
                break;
            case "PREPARE":
                // System.out.println("received prepare: " + msg.toString());
                _ibft.receivePrepare(msg);
                break;
            case "COMMIT":
                // System.out.println("received commit: " + msg.toString());
                _ibft.receiveCommit(msg);
                break;
            case "CREATE":
                _ibft.createAccount(msg.getPublicKey(), clientPort);
                break;
            case "S_BALANCE":
                _ibft.strongCheckBalance(msg.getPublicKey(), clientPort, msg.getLastRead());
                break;
            case "W_BALANCE":
                System.out.println("Received weak balance check");
                _ibft.weakCheckBalance(msg.getPublicKey(), clientPort);
                break;
            case "TRANSACTION":
                _ibft.makeTransaction(msg.getValue(), clientPort);
                break;
            default:
                System.out.println("Unknown message type: " + type);
                break;
        }
    }

    /**
     * sends a message to every server
     */
    public void sendBroadcast(Message msg) {
        try {
            InetAddress destAddr = InetAddress.getByName("127.0.0.1");
            for (int i = 1; i <= _broadcastNeighbors.size(); i++) { // send to all servers
                _PAChannel.sendMessage(destAddr, _broadcastNeighbors.get(i), msg);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } // just localhost
    }

    public void sendDecide(DecidedMessage msg, int destPort) {
        try {
            InetAddress destAddr = InetAddress.getByName("127.0.0.1");
            msg.setId(_operationId++);
            _PAChannel.sendDecide(destAddr, destPort, msg);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public String getSnapshotSignature(String accountsStrings) {
        return _PAChannel.signSnapshotBlock(accountsStrings);
    }

    public Boolean verifySignature(String signature, String expected, int serverId){
        return _PAChannel.verifySignature(signature, expected, serverId);
    }
}