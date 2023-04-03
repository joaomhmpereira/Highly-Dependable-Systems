package sec.G31;

import java.security.PublicKey;

public class Account {

    private PublicKey _publicKey;
    private int _balance;
    private int _tempBalance; // during the validation of a block the balance may be slightly different 

    public Account(PublicKey publicKey, int balance) {
        _publicKey = publicKey;
        _balance = balance;
        _tempBalance = balance;
    }

    public PublicKey getPublicKey() {
        return _publicKey;
    }

    public int getBalance() {
        return _balance;
    }

    public int getTempBalance(){
        return _tempBalance;
    }

    public void addBalance(int amount) {
        _balance += amount;
    }

    public void addTempBalance(int amount){
        _tempBalance += amount;
    }

    public void subtractBalance(int amount) {
        _balance -= amount;
    }   

    public void subtractTempBalance(int amount){
        _tempBalance -= amount;
    }

    public boolean canSubtractBalance(int amount){
        return _balance >= amount;
    }

    public boolean canSubtractBalanceBlockchain(int amount){
        return _tempBalance >= amount;
    }

    @Override
    public String toString() {
        return ":::Account PubKey -> " + _publicKey.toString() + ", Balance -> " + _balance + " :::";
    }
}
