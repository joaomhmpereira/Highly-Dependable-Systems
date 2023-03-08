package sec.G31.messages;

import java.io.Serializable;

public class Message implements Serializable {
    private String _type;
    private String _broadcastType;
    private int _instance;
    private int _round;
    private int _sequenceNumber;
    private String _message;

    public Message(String _type, int instance, int sequenceNumber, int round, String message){
        _sequenceNumber = sequenceNumber;
        _message = message;
        _round = round;
        _instance = instance;
    }

    public String getType(){
        return _type;
    }

    public int getSequenceNumber(){
        return _sequenceNumber;
    }

    public String getMessage(){
        return _message;
    }

    @Override
    public String toString(){
        return "PrepareMessage: No: " + _sequenceNumber + " Message: " + _message;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Message)){
            return false;
        }
        Message msg = (Message) obj;
        return msg._sequenceNumber == _sequenceNumber && msg._message.equals(_message) && msg._type.equals(_type);
    }
}
