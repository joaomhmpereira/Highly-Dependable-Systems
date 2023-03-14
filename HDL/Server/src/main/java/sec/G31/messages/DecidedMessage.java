package sec.G31.messages;

import java.io.Serializable;

public class DecidedMessage implements Serializable {
    
    private String _msg;
    private int _senderId;
    private String _cipheredDigest;

    public DecidedMessage(String msg, int senderId) {
        _senderId = senderId;
        _msg = msg;
    }

    public String getMsg() {
        return _msg;
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
}
