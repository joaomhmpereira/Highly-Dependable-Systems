package sec.G31.messages;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

import sec.G31.Account;

public class DecidedMessage implements Serializable {
    private String _type;
    private float _balance;
    private String _value;
    private int _senderId;
    private int _id;
    private String _cipheredDigest;
    private int _nonce;
    private Hashtable<Integer, String> _signatures;
    private List<Account> _accounts;
    //private Hashtable<PublicKey, Account> _accounts;

    public DecidedMessage(String type, String value, int senderId, int nonce) {
        _type = type;
        _senderId = senderId;
        _value = value;
        _nonce = nonce;
        _balance = -1;
        _signatures = null;
        _accounts = null;
    }

    public DecidedMessage(String type, float balance, int senderId, int nonce) {
        _type = type;
        _senderId = senderId;
        _balance = balance;
        _id = -1;
        _nonce = nonce;
        _value = "";
        _signatures = null;
        _accounts = null;
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

    //public void setAccounts(Hashtable<PublicKey, Account> accounts) {
    //    _accounts = accounts;
    //}
//
    //public Hashtable<PublicKey, Account> getAccounts() {
    //    return _accounts;
    //}

    public void setAccounts(List<Account> accounts) {
        _accounts = accounts;
    }

    public List<Account> getAccounts() {
        return _accounts;
    }

    public String getCipheredDigest() {
        return _cipheredDigest;
    }

    public void setSignatures(Hashtable<Integer, String> signatures) {
        _signatures = signatures;
    }

    public Hashtable<Integer, String> getSignatures() {
        return _signatures;
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
        if (_signatures != null){
            return other.getValue().equals(_value) && other.getSenderId() == _senderId 
            && other.getId() == _id && other.getType().equals(_type) 
            && other.getBalance() == _balance && other.getNonce() == _nonce 
            && other.getSignatures().equals(_signatures)
            && other.getAccounts().equals(_accounts);
        } else {
            return other.getValue().equals(_value) && other.getSenderId() == _senderId 
            && other.getId() == _id && other.getType().equals(_type) 
            && other.getBalance() == _balance && other.getNonce() == _nonce;
        }   
    }

    @Override
    public String toString() {
        return _type + "." + _value + "." + _id; // not sure about this one
    }
}
