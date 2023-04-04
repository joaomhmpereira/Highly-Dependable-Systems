package sec.G31.messages;

import java.io.Serializable;

public class DecidedMessage implements Serializable {
    private String _type;
    private float _balance;
    private String _value;
    private int _senderId;
    private int _id;
    private String _cipheredDigest;
    private int _nonce;

    public DecidedMessage(String type, String value, int senderId, int nonce) {
        _type = type;
        _senderId = senderId;
        _value = value;
        _nonce = nonce;
        _balance = -1;
    }

    public DecidedMessage(String type, float balance, int senderId, int nonce) {
        _type = type;
        _senderId = senderId;
        _balance = balance;
        _id = -1;
        _nonce = nonce;
        _value = "";
    }

    public float getBalance() {
        return _balance;
    }

    public String getType() {
        return _type;
    }

    public int getNonce() {
        return _nonce;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getValue() {
        return _value;
    }

    public int getSenderId() {
        return _senderId;
    }

    public String getCipheredDigest() {
        return _cipheredDigest;
    }

    public void setCipheredDigest(String cipheredDigest) {
        _cipheredDigest = cipheredDigest;
    }

    public String stringForDigest(){
        return _value + "." + _senderId + "." + _id + "." + _type + "." + _balance + "." + _nonce;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DecidedMessage)) {
            return false;
        }
        DecidedMessage other = (DecidedMessage) obj;
        return other.getValue().equals(_value) && other.getSenderId() == _senderId 
            && other.getId() == _id && other.getType().equals(_type) 
            && other.getBalance() == _balance && other.getNonce() == _nonce;
    }

    @Override
    public String toString() {
        return _type + "." + _value + "." + _id; // not sure about this one
    }
}
