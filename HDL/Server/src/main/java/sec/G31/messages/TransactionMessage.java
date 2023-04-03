package sec.G31.messages;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Base64;

public class TransactionMessage implements Serializable {

    private PublicKey _source;
    private PublicKey _destination;
    private int _amount;
    private int _clientPort; // to know to which client to send after transaction commited 

    public TransactionMessage(PublicKey source, PublicKey destination, int amount) {
        _source = source;
        _destination = destination;
        _amount = amount;
    }

    public PublicKey getSource() {
        return _source;
    }

    public PublicKey getDestination() {
        return _destination;
    }

    public int getAmount() {
        return _amount;
    }

    public void setClientPort(int clientPort){
        _clientPort = clientPort;
    }

    public int getClientPort(){
        return _clientPort;
    }

    @Override
    public String toString() {
        byte[] sourceBytes = _source.getEncoded();
        byte[] destBytes = _destination.getEncoded();
        String b46source = Base64.getEncoder().encodeToString(sourceBytes);
        String b46dest = Base64.getEncoder().encodeToString(destBytes);
        return "TransactionMessage{" +
                "source=" + b46source +
                ", destination=" + b46dest +
                ", amount=" + _amount +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!TransactionMessage.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final TransactionMessage other = (TransactionMessage) obj;
        return this._destination.equals(other._destination) && this._source.equals(other._source) && this._amount == other._amount && this._clientPort == other._clientPort;
    }
}