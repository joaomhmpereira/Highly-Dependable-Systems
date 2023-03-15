package sec.G31.messages;

import java.io.Serializable;

public class AckMessage implements Serializable {
    
    // server -> server or server -> client, message received 
    private Message _ackedMessage; 
    // client -> server, decided message received 
    private DecidedMessage _ackedDecidedMessage; 
    // the entity that is acknowledging the message (client or server)
    private int _senderId;


    public AckMessage(Message msg, int senderId){
        _ackedMessage = msg;
        _senderId = senderId;
    }

    public AckMessage(DecidedMessage msg, int senderId){
        _ackedDecidedMessage = msg;
        _senderId = senderId;
    }

    public Message getAckedMessage(){
        return _ackedMessage;
    }

    public DecidedMessage getAckedDecidedMessage(){
        return _ackedDecidedMessage;
    }

    public int getSenderId(){
        return _senderId;
    }

    public boolean isDecidedMessage(){
        return _ackedDecidedMessage != null;
    }

    @Override
    public String toString(){
        if (_ackedDecidedMessage != null)
            return "AckMessage: " + _ackedDecidedMessage.toString() + " from " + _senderId;
        else
            return "AckMessage: " + _ackedMessage.toString() + " from " + _senderId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AckMessage)) {
            return false;
        }
        AckMessage other = (AckMessage) obj;
        if (_ackedDecidedMessage != null)
            return other.getAckedDecidedMessage().equals(_ackedDecidedMessage) && other.getSenderId() == _senderId;
        else
            return other.getAckedMessage().equals(_ackedMessage) && other.getSenderId() == _senderId;
    }
}
