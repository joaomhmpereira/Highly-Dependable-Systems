package sec.G31.messages;

import java.io.Serializable;

public class Message implements Serializable {
    private String _type;
    private String _broadcastType;
    private int _instance;
    private int _round;
    //private int _sequenceNumber;
    private String _value;
    private int _senderId;

    public Message(String _type, int instance, int round, String value, int senderId){
        //_sequenceNumber = sequenceNumber;
        _value = value;
        _round = round;
        _instance = instance;
        _senderId = senderId;
    }

    public String getType(){
        return _type;
    }

    //public int getSequenceNumber(){
    //    return _sequenceNumber;
    //}

    public String getValue(){
        return _value;
    }
    
    public int getSenderId(){
        return _senderId;
    }

    public int getRound(){
        return _round;
    }

    public int getInstance(){
        return _instance;
    }

    @Override
    public String toString(){
        return "PrepareMessage: Message: " + _value + " Round: " + _round + " Instance: " + _instance + " Sender: " + _senderId;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Message)){
            return false;
        }
        Message msg = (Message) obj;
        return msg._value.equals(_value) && msg._type.equals(_type)
         && msg._round == _round && msg._instance == _instance && msg._senderId == _senderId;
    }
}
