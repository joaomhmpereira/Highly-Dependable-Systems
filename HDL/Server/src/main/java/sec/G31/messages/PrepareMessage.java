package sec.G31.messages;

public class PrepareMessage {
    
    private int _sequenceNumber;
    private String _message;

    public PrepareMessage(int sequenceNumber, String message){
        _sequenceNumber = sequenceNumber;
        _message = message;
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
}
