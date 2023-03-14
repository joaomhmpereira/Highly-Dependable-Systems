package sec.G31.messages;

import java.io.Serializable;

public class DecidedMessage implements Serializable {
    private String _value;
    private int _senderId;
    private String _cipheredDigest;

    public DecidedMessage(String value, int senderId) {
        _senderId = senderId;
        _value = value;
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
        return _value + "." + _senderId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DecidedMessage)) {
            return false;
        }
        DecidedMessage other = (DecidedMessage) obj;
        return other.getValue().equals(_value) && other.getSenderId() == _senderId;
    }
}
