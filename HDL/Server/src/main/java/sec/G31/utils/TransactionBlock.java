package sec.G31.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import sec.G31.Account;
import sec.G31.messages.TransactionMessage;

public class TransactionBlock implements Serializable {
    
    private List<TransactionMessage> _transactions;
    //private Hashtable<PublicKey, Account> _accounts;
    private List<Account> _accounts;
    private Hashtable<Integer, String> _signatures;
    private String _type;
    

    public TransactionBlock(String type) {
        _type = type;
        if (_type.equals("SNAPSHOT")){
            //_accounts = new Hashtable<PublicKey, Account>();
            _accounts = new ArrayList<Account>();
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

    public String getType(){
        return _type;
    }

    //public Hashtable<PublicKey, Account> getAccounts(){
    //    return _accounts;
    //}
//
    //public void addAccounts(Hashtable<PublicKey, Account> accounts){
    //    _accounts = accounts;
    //}

    public List<Account> getAccounts(){
        return _accounts;
    }

    public void setAccounts(List<Account> accounts){
        _accounts = accounts;
    }

    public Hashtable<Integer, String> getSignatures(){
        return _signatures;
    }

    public void addSignatures(Hashtable<Integer, String> signatures){
        _signatures = signatures;
    }
    
    public boolean containsTransaction(TransactionMessage transaction){
        return _transactions.contains(transaction);
    }

    public boolean isCompleted(){
        return _transactions.size() == 2;
    }

    @Override
    public String toString(){
        if (_type.equals("SNAPSHOT")){
            String result = ":::Snapshot Block:::\n";
            result += _accounts.toString() + "\n";
            result += ":::End of Snapshot Block:::";
            return result;
        } else {
            String result = ":::Transaction Block:::\n";
            for (TransactionMessage transaction : _transactions) {
                result += transaction.toString() + "\n";
            }
            result += ":::End of Transaction Block:::";
            return result;
        }
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
        if (_accounts != null){
            return this._accounts.equals(other._accounts) && this._signatures.equals(other._signatures);
        }
        return this._transactions.equals(other._transactions);
    }
}
