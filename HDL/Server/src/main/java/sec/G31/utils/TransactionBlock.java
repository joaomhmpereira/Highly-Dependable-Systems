package sec.G31.utils;

import java.util.ArrayList;
import java.util.List;

import sec.G31.messages.TransactionMessage;

public class TransactionBlock {
    
    List<TransactionMessage> _transactions;

    public TransactionBlock() {
        _transactions = new ArrayList<TransactionMessage>(10);
    }

    public void addTransaction(TransactionMessage transaction) {
        _transactions.add(transaction);
    }
    
    public boolean containsTransaction(TransactionMessage transaction){
        return _transactions.contains(transaction);
    }

    public boolean isCompleted(){
        return _transactions.size() == 10;
    }

    @Override
    public String toString(){
        String result = ":::Transaction Block:::\n";
        for (TransactionMessage transaction : _transactions) {
            result += transaction.toString() + "\n";
        }
        result += ":::End of Transaction Block:::";
        return result;
    }
}
