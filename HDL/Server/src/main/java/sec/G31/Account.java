package sec.G31;

import java.security.PublicKey;

public class Account {

    private PublicKey _publicKey;
    private int _balance;

    public Account(PublicKey publicKey, int balance) {
        _publicKey = publicKey;
        _balance = balance;
    }

    public PublicKey getPublicKey() {
        return _publicKey;
    }

    public int getBalance() {
        return _balance;
    }

    public void addBalance(int amount) {
        _balance += amount;
    }

    public boolean subtractBalance(int amount) {
        if (_balance - amount >= 0){
            _balance -= amount;
            return true;
        }
        return false;
    }   

    @Override
    public String toString() {
        return ":::Account PubKey -> " + _publicKey.toString() + ", Balance -> " + _balance + " :::";
    }
}
