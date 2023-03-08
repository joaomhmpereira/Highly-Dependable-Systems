package sec.G31;
import java.util.logging.Logger;

import sec.G31.messages.Message;

import java.net.*;
import java.util.Hashtable;

// nao sei se vai ter que ser uma thread actually 

/**
 * TO-DO:
 *  + associate the doubleEchoBroadcast to an actual message
 *  + Don't use strings to send the message i think, or use them very strictly
 */

public class DoubleEchoBroadcast
{ 
    private String msg; // A broadcast is associated to a msg
    private PerfectAuthChannel _channel; // the channel that it uses for communication
    private Hashtable<Integer, Integer> _broadcastNeighbors; // to send broadcast
    private int _leader; 
    private int _F; 
    private int _numNeighbours;
    private int _N;
    private Server _server;
    private boolean _sentecho;
    private boolean _sentready;
    private boolean _delivered;
    private int[] _echos; // list because we must know the ones that already acked so far 
    private int _echosReceived; // to know how many have acked efficiently
    private int[] _readys; // list because we must know the ones that already acked so far 
    private int _readysReceived; // to know how many have acked efficiently

    // _broadcast = _server.getPerfectchannel()



    /**
     * TO-DO: o double Echo Broadcast deve usar os canais que ja estao construidos para mandar as mensagens.
     */
    public DoubleEchoBroadcast(String msg, Server server, Hashtable<Integer, Integer> neighbours, int leader, int faulties, int numServers){

        // TO-DO the perfectAuthChannel must be linked to the IBFT and not the server
        _channel = new PerfectAuthChannel(server, server.getAddress(), server.getPort()); 
        // _channel.setNewDoubleEcho(this)
        _numNeighbours = neighbours.size();
        _broadcastNeighbors = neighbours;
        _server = server;
        _leader = leader;
        _F = faulties;
        _N = numServers;

        // algorithm specs
        _sentecho = false;
        _sentready = false;
        _delivered = false;
        _echos = new int[_numNeighbours]; // starts with all to 0
        _readys = new int[_numNeighbours]; // start with all to 0
    }

    /** 
     * enviar o SEND
     */
    public void broadcastSEND(){

        if(_server.getId() != _leader){return; } // check if the process is the leader 
        String msg = "SEND" + _server.getId();
        //sendBroadcastMsg(msg); // send the broadcast
    } 

    /**
     * temos que determinar nas layers mais abaixo como e que diferenciamos 
     * estas mensagens das outras mensagens.
     * 
     * Vou assumir que apenas o lider e que pode mandar estas mensagens
     */
    public void broadcastReceiveSEND(String msg, int senderId){

        if(!(senderId == _leader)){return; }
        if(_sentecho = true){return; }

        _sentecho = true;
        String txt = "ECHO " + _server.getId();
        //sendBroadcastMsg(txt);
    }

    public void broadcastReceiveECHO(int senderId){

        if(_echos[senderId] != 0){ return; } // already acked
        
        _echos[senderId] = 1;  // updating echo received from that fella
        _echosReceived++; // updated number of echo's received

        int quorum = (_N-_F)/2;
        if(_sentready == false && _echosReceived > quorum){
            _sentready = true;
            String msg = "READY " + _server.getId();
            //sendBroadcastMsg(msg);
        }
    }


    public void broadcastReceivedREADY(int senderId){

        if(_readys[senderId] != 0){ return; } // already acked

        _readys[senderId] = 1;
        _readysReceived++;

        if(_sentready == false && _readysReceived > _F){ // he didn't knew there was already a quorum 
            _sentready = true; 
            msg = "READY " + _server.getId();
            //sendBroadcastMsg(msg);
        }

        if(_readysReceived > 2*_F && _delivered == false){
            _delivered = true;
            
            // TO-DO, a mensagem m foi delivered
            // IBFT.deliveredMsg(msg);
        }
    }


    /**
     * sends a message to every server
     */
    public void sendBroadcastMsg(Message msg){
        // send message to all
        InetAddress destAddr;
        try {
            destAddr = InetAddress.getByName("127.0.0.1");
            for(int i=0; i<_numNeighbours; i++){ // send to all servers 
                _channel.sendMessage(destAddr, _broadcastNeighbors.get(i), msg);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } // just localhost
    }
}
