package sec.G31.messages;

import java.io.Serializable;

public class InitInstance implements Serializable {
    
    private String _value;
    private int _clientId;
    private String _cipheredDigest;

    public InitInstance(int clientId, String value) {
        _value = value;
        _clientId = clientId;
    }

    public String getValue() {
        return _value;
    }

    public int getClientId() {
        return _clientId;
    }

    public String getCipheredDigest() {
        return _cipheredDigest;
    }

    public void setCipheredDigest(String cipheredDigest) {
        _cipheredDigest = cipheredDigest;
    }

    public String stringForDigest() {
        return _value + "." + _clientId;
    }

    @Override
    public String toString() {
        return "Value: " + _value + " ClientId: " + _clientId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InitInstance)) {
            return false;
        }
        InitInstance other = (InitInstance) obj;
        return _value.equals(other._value) && _clientId == other._clientId && _cipheredDigest.equals(other._cipheredDigest);
    }
        
}
