package sec.G31.messages;

import java.io.Serializable;
import java.security.PublicKey;

public class TransactionMessage implements Serializable {

    private PublicKey _source;
    private PublicKey _destination;
    private int _amount;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!TransactionMessage.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final TransactionMessage other = (TransactionMessage) obj;
        return this._destination.equals(other._destination) && this._source.equals(other._source) && this._amount == other._amount;
    }
}