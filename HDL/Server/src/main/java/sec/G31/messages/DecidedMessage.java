package sec.G31.messages;

import java.io.Serializable;

public class DecidedMessage implements Serializable {
    private String _type;
    private int _balance;
    private String _value;
    private int _senderId;
    private int _instance;
    private String _cipheredDigest;
    private int _nonce;

    public DecidedMessage(String type, String value, int senderId, int instance, int nonce) {
        _type = type;
        _senderId = senderId;
        _value = value;
        _instance = instance;
        _nonce = nonce;
        _balance = -1;
    }

    public DecidedMessage(String type, int balance, int senderId, int nonce) {
        _type = type;
        _senderId = senderId;
        _balance = balance;
        _instance = -1;
        _nonce = nonce;
        _value = "";
    }

    public int getBalance() {
        return _balance;
    }

    public String getType() {
        return _type;
    }

    public int getNonce() {
        return _nonce;
    }

    public int getInstance() {
        return _instance;
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
        return _value + "." + _senderId + "." + _instance + "." + _type + "." + _balance + "." + _nonce;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DecidedMessage)) {
            return false;
        }
        DecidedMessage other = (DecidedMessage) obj;
        return other.getValue().equals(_value) && other.getSenderId() == _senderId 
            && other.getInstance() == _instance && other.getType().equals(_type) 
            && other.getBalance() == _balance && other.getNonce() == _nonce;
    }
}
