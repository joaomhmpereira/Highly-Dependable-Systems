package sec.G31.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sec.G31.messages.TransactionMessage;

public class TransactionBlock implements Serializable {
    
    List<TransactionMessage> _transactions;

    public TransactionBlock() {
        _transactions = new ArrayList<TransactionMessage>(2);
    }

    public void addTransaction(TransactionMessage transaction) {
        _transactions.add(transaction);
    }

    public List<TransactionMessage> getTransactions() {
        return _transactions;
    }
    
    public boolean containsTransaction(TransactionMessage transaction){
        return _transactions.contains(transaction);
    }

    public boolean isCompleted(){
        return _transactions.size() == 2;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!TransactionBlock.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final TransactionBlock other = (TransactionBlock) obj;
        return this._transactions.equals(other._transactions);
    }

    @Override
    public int hashCode() {
        return _transactions.hashCode();
    }
}
