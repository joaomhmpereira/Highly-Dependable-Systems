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

    @Override
    public String toString() {
        return ":::Account PubKey -> " + _publicKey.toString() + ", Balance -> " + _balance + " :::";
    }
}
