package sec.G31;

import java.io.Serializable;
import java.security.PublicKey;

public class Account implements Serializable {

    private PublicKey _publicKey;
    private float _balance;
    private float _tempBalance; // during the validation of a block the balance may be slightly different 

    public Account(PublicKey publicKey, float balance) {
        _publicKey = publicKey;
        _balance = balance;
        _tempBalance = balance;
    }

    public PublicKey getPublicKey() {
        return _publicKey;
    }

    public float getBalance() {
        return _balance;
    }

    public float getTempBalance(){
        return _tempBalance;
    }

    public void addBalance(float amount) {
        _balance += amount;
    }

    public void addTempBalance(float amount){
        _tempBalance += amount;
    }

    public void subtractBalance(float amount) {
        _balance -= amount;
    }   

    public void subtractTempBalance(float amount){
        _tempBalance -= amount;
    }

    public boolean canSubtractBalance(float amount){
        return _balance >= amount;
    }

    public boolean canSubtractBalanceBlockchain(float amount){
        return _tempBalance >= amount;
    }

    @Override
    public String toString() {
        return ":::Account PubKey -> " + _publicKey.toString() + ", Balance -> " + _balance + " :::";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Account other = (Account) obj;
        if (this._publicKey != other._publicKey && (this._publicKey == null || !this._publicKey.equals(other._publicKey))) {
            return false;
        }
        if (Float.floatToIntBits(this._balance) != Float.floatToIntBits(other._balance)) {
            return false;
        }
        return true;
    }
}
