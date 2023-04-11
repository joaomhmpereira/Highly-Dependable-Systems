package sec.G31.utils;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import sec.G31.Account;
import sec.G31.messages.TransactionMessage;

public class TransactionBlock implements Serializable {
    
    List<TransactionMessage> _transactions;
    Hashtable<PublicKey, Account> _accounts;
    Hashtable<Integer, String> _signatures; 
    

    public TransactionBlock(String type) {
        if (type.equals("SNAPSHOT BLOCK")){
            _accounts = new Hashtable<PublicKey, Account>();
            _signatures = new Hashtable<Integer, String>();
        }
        else {
            _transactions = new ArrayList<TransactionMessage>(2);
            _accounts = null;
            _signatures = null;
        }
    }

    public void addTransaction(TransactionMessage transaction) {
        _transactions.add(transaction);
    }

    public List<TransactionMessage> getTransactions() {
        return _transactions;
    }

    public void addAccounts(Hashtable<PublicKey, Account> accounts){
        _accounts = accounts;
    }

    public void addSignature(int id, String signature){
        _signatures.put(id, signature);
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
        if (!_accounts.equals(null)){
            return this._accounts.equals(other._accounts) && this._signatures.equals(other._signatures);
        }
        return this._transactions.equals(other._transactions);
    }

    @Override
    public int hashCode() {
        return _transactions.hashCode();
    }
}
