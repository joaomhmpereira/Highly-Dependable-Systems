package sec.G31.messages;

import java.io.Serializable;
import java.security.PublicKey;

import sec.G31.utils.TransactionBlock;

public class Message implements Serializable {
    private String _type;
    private int _instance;
    private int _round;
    private TransactionMessage _value;
    private TransactionBlock _block;
    private int _senderId;
    private int _senderPort;
    private int _nonce; // to differentiate equal messages
    private int _lastRead; // to know which messages have been read by the client
    private PublicKey _publicKey;
    private String _cipheredDigest;
    private String _snapshotSignature;

    // server -> server
    public Message(String type, int instance, int round, TransactionBlock block, int senderId, int senderPort, int nonce) {
        _type = type;
        _block = block;
        _round = round;
        _instance = instance;
        _senderId = senderId;
        _senderPort = senderPort;
        _nonce = nonce;
        _publicKey = null;
        _value = null;
        _lastRead = -1;
    }

    // client -> server, a transaction message
    public Message(String type, TransactionMessage value, int senderId, int senderPort, int nonce) {
        _type = type;
        _value = value;
        _round = -1;
        _instance = -1;
        _senderId = senderId;
        _senderPort = senderPort;
        _nonce = nonce;
        _publicKey = null;
        _block = null;
        _lastRead = -1;
    }

    // client -> server, a check balance/create account message
    public Message(String type, int senderId, int senderPort, int nonce, PublicKey publicKey, int lastRead) {
        _type = type;
        _value = null;
        _round = -1;
        _instance = -1;
        _senderId = senderId;
        _senderPort = senderPort;
        _nonce = nonce;
        _publicKey = publicKey;
        _block = null;
        _lastRead = lastRead;
    }

    public String getType() {
        return _type;
    }

    public TransactionBlock getBlock() {
        return _block;
    }

    public boolean isBlockSet() {
        return _block != null;
    }

    public TransactionMessage getValue() {
        return _value;
    }

    public int getSenderId() {
        return _senderId;
    }

    public int getRound() {
        return _round;
    }

    public int getInstance() {
        return _instance;
    }

    public int getSenderPort() {
        return _senderPort;
    }

    public void setInstance(int instance) {
        _instance = instance;
    }

    public int getNonce() {
        return _nonce;
    }

    public int getLastRead() {
        return _lastRead;
    }

    public String getCipheredDigest() {
        return _cipheredDigest;
    }

    public PublicKey getPublicKey() {
        return _publicKey;
    }

    public String getSnapshotSignature() {
        return _snapshotSignature;
    }

    public void setSnapshotSignature(String snapshotSignature) {
        _snapshotSignature = snapshotSignature;
    }

    public void setCipheredDigest(String cipheredDigest) {
        _cipheredDigest = cipheredDigest;
    }

    public String stringForDigest() {
        if (_publicKey != null) // check balance/create account message
            return _type + "." + _senderId + "." + _senderPort + "." + _nonce + "." + _lastRead;
        else if (this.isBlockSet()) // transaction block message
            return _type + "." + _block.toString() + "." + _round + "." + _instance + "." + _senderId + "."
                    + _senderPort + "." + _nonce;
        else // transaction message
            return _type + "." + _value.toString() + "." + _round + "." + _instance + "." + _senderId + "."
                    + _senderPort + "." + _nonce;
    }

    @Override
    public String toString() {
        if (_publicKey != null) // check balance/create account message
            return _type + "." + _senderId + "." + _senderPort + "." + _nonce + "." + _lastRead;
        else if (this.isBlockSet()) // transaction block message
            return _type + "." + _round + "." + _instance + "." + _senderId + "."
                    + _senderPort + "." + _nonce;
        else // transaction message
            return _type + "." + _value.toString() + "." + _round + "." + _instance + "." + _senderId + "."
                    + _senderPort + "." + _nonce;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Message)) {
            return false;
        }
        Message msg = (Message) obj;
        if (_value != null) {
            return msg.getType().equals(_type) && msg.getValue().equals(_value)
                    && msg.getRound() == _round && msg.getInstance() == _instance
                    && msg.getSenderId() == _senderId && msg.getSenderPort() == _senderPort
                    && msg.getNonce() == _nonce && msg.getLastRead() == _lastRead;
        } else if (this.isBlockSet()) {
            //System.out.println(msg.toString());
            //System.out.println(this.toString());
            return msg.getType().equals(_type) && msg.getBlock().equals(_block)
                    && msg.getRound() == _round && msg.getInstance() == _instance
                    && msg.getSenderId() == _senderId && msg.getSenderPort() == _senderPort
                    && msg.getNonce() == _nonce;
        } else{
            return msg.getType().equals(_type) && msg.getSenderId() == _senderId
            && msg.getSenderPort() == _senderPort && msg.getNonce() == _nonce;
        }    
    }
}
