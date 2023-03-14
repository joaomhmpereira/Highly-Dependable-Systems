package sec.G31.messages;

import java.io.Serializable;

public class Message implements Serializable {
    private String _type;
    private int _instance;
    private int _round;
    private String _value;
    private int _senderId;
    private int _senderPort;
    private String _cipheredDigest;

    public Message(String type, int instance, int round, String value, int senderId, int senderPort) {
        _type = type;
        _value = value;
        _round = round;
        _instance = instance;
        _senderId = senderId;
        _senderPort = senderPort;
    }

    public String getType() {
        return _type;
    }

    public String getValue() {
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

    public int getSenderPort(){
        return _senderPort;
    }

    public String getCipheredDigest() {
        return _cipheredDigest;
    }

    public void setCipheredDigest(String cipheredDigest) {
        _cipheredDigest = cipheredDigest;
    }

    public String stringForDigest() {
        return _type + "." + _value + "." + _round + "." + _instance + "." + _senderId + "." + _senderPort;
    }

    @Override
    public String toString() {
        return _type + " Value: " + _value + " Round: " + _round + " Instance: " + _instance + " Sender: " + _senderId + " Port: " + _senderPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Message)) {
            return false;
        }
        Message msg = (Message) obj;
        return msg._value.equals(_value) && msg._type.equals(_type)
                && msg._round == _round && msg._instance == _instance && msg._senderId == _senderId;
    }
}
