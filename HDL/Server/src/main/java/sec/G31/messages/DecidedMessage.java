package sec.G31.messages;

import java.io.Serializable;

public class DecidedMessage implements Serializable {
    private String _value;
    private int _senderId;
    private int _instance;
    private String _cipheredDigest;

    public DecidedMessage(String value, int senderId, int instance) {
        _senderId = senderId;
        _value = value;
        _instance = instance;
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
        return _value + "." + _senderId + "." + _instance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DecidedMessage)) {
            return false;
        }
        DecidedMessage other = (DecidedMessage) obj;
        return other.getValue().equals(_value) && other.getSenderId() == _senderId && other.getInstance() == _instance;
    }
}
