package sec.G31.client;

import static org.junit.Assert.*;
//import org.junit.jupiter.api.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import sec.G31.*;
import sec.G31.messages.TransactionMessage;
import sec.G31.utils.TransactionBlock;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppClientTest {

    private PublicKey client1PublicKey;
    private PublicKey client2PublicKey;

    // @BeforeAll
    public void setupKeys() {
        try {
            client1PublicKey = readPublicKey("../keys/clients/1/public_key.der");
            client2PublicKey = readPublicKey("../keys/clients/2/public_key.der");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fourCorrectServers() {
        System.out.println("::::::::::::::::::::::::::::::::::::::::: TEST 1 :::::::::::::::::::::::::::::::::::::::::::::::");
        this.setupKeys();

        Blockchain blockchain = new Blockchain();

        final TransactionMessage t1 = new TransactionMessage(client2PublicKey, client1PublicKey, 15.0f);
        final TransactionMessage t2 = new TransactionMessage(client1PublicKey, client2PublicKey, 30.0f);

        final TransactionBlock block = new TransactionBlock("TRANSACTIONS");
        block.addTransaction(t1);
        block.addTransaction(t2);
        blockchain.addTransactionBlock(block);

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 4446, "NF", "N", 1, "../configs/config1_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 4447, "NF", "N", 1, "../configs/config1_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 4448, "NF", "N", 1, "../configs/config1_4.txt");
        App leader = new App(1, "127.0.0.1", 4445, "NF", "Y", 1, "../configs/config1_4.txt");
        AppClient client1 = new AppClient(1, "../configs/config1_4.txt", "127.0.0.1", 9555, 1);
        AppClient client2 = new AppClient(2, "../configs/config1_4.txt", "127.0.0.1", 9556, 1);

        // client.submitValue(valueToDecide);
        client1.createAccount(client1PublicKey);
        client2.createAccount(client2PublicKey);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client2.submitTransaction(client2PublicKey, client1PublicKey, 15.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 30.0f);

        // Wait for the value to be decided
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(blockchain, leader.getServer().getBlockchain());
        assertEquals(blockchain, secondServer.getServer().getBlockchain());
        assertEquals(blockchain, thirdServer.getServer().getBlockchain());
        assertEquals(blockchain, fourthServer.getServer().getBlockchain());
    }

    @Test
    public void threeCorrectOneFaulty() {
        System.out.println("::::::::::::::::::::::::::::::::::::::::: TEST 2 :::::::::::::::::::::::::::::::::::::::::::::::");
        this.setupKeys();

        Blockchain blockchain = new Blockchain();

        final TransactionMessage t1 = new TransactionMessage(client2PublicKey, client1PublicKey, 15.0f);
        final TransactionMessage t2 = new TransactionMessage(client1PublicKey, client2PublicKey, 30.0f);

        final TransactionBlock block = new TransactionBlock("TRANSACTIONS");
        block.addTransaction(t1);
        block.addTransaction(t2);
        blockchain.addTransactionBlock(block);

        // Create 4 servers (server with id 4 is faulty)
        App leader = new App(1, "127.0.0.1", 5445, "NF", "Y", 1, "../configs/config2_4.txt");
        App secondServer = new App(2, "127.0.0.1", 5446, "NF", "N", 1, "../configs/config2_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 5447, "NF", "N", 1, "../configs/config2_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 5448, "F", "N", 1, "../configs/config2_4.txt");
        AppClient client1 = new AppClient(1, "../configs/config2_4.txt", "127.0.0.1", 9557, 1);
        AppClient client2 = new AppClient(2, "../configs/config2_4.txt", "127.0.0.1", 9558, 1);

        client1.createAccount(client1PublicKey);
        client2.createAccount(client2PublicKey);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client2.submitTransaction(client2PublicKey, client1PublicKey, 15.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 30.0f);

        // Wait for the value to be decided
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(blockchain, leader.getServer().getBlockchain());
        assertEquals(blockchain, secondServer.getServer().getBlockchain());
        assertEquals(blockchain, thirdServer.getServer().getBlockchain());
        assertEquals(blockchain, fourthServer.getServer().getBlockchain());
    }

    /**
     * Unit test: 2 consensus instances with 4 correct servers
     */
    @Test
    public void twoConsensusInstancesAllCorrect() {
        System.out.println("::::::::::::::::::::::::::::::::::::::::: TEST 3 :::::::::::::::::::::::::::::::::::::::::::::::");
        this.setupKeys();

        Blockchain blockchain = new Blockchain();

        final TransactionMessage t1 = new TransactionMessage(client2PublicKey, client1PublicKey, 15.0f);
        final TransactionMessage t2 = new TransactionMessage(client1PublicKey, client2PublicKey, 30.0f);
        final TransactionMessage t3 = new TransactionMessage(client2PublicKey, client1PublicKey, 25.0f);
        final TransactionMessage t4 = new TransactionMessage(client1PublicKey, client2PublicKey, 5.0f);

        final TransactionBlock block1 = new TransactionBlock("TRANSACTIONS");
        final TransactionBlock block2 = new TransactionBlock("TRANSACTIONS");
        block1.addTransaction(t1);
        block1.addTransaction(t2);
        block2.addTransaction(t3);
        block2.addTransaction(t4);
        blockchain.addTransactionBlock(block1);
        blockchain.addTransactionBlock(block2);

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 7446, "NF", "N", 1, "../configs/config3_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 7447, "NF", "N", 1, "../configs/config3_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 7448, "NF", "N", 1, "../configs/config3_4.txt");
        App leader = new App(1, "127.0.0.1", 7445, "NF", "Y", 1, "../configs/config3_4.txt");
        AppClient client1 = new AppClient(1, "../configs/config3_4.txt", "127.0.0.1", 9559, 1);
        AppClient client2 = new AppClient(2, "../configs/config3_4.txt", "127.0.0.1", 9560, 1);

        // client.submitValue(valueToDecide1);
        // client.submitValue(valueToDecide2);
        client1.createAccount(client1PublicKey);
        client2.createAccount(client2PublicKey);

        client2.submitTransaction(client2PublicKey, client1PublicKey, 15.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 30.0f);

        client2.submitTransaction(client2PublicKey, client1PublicKey, 25.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 5.0f);

        // Wait for the value2 to be decided
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(blockchain, leader.getServer().getBlockchain());
        assertEquals(blockchain, secondServer.getServer().getBlockchain());
        assertEquals(blockchain, thirdServer.getServer().getBlockchain());
        assertEquals(blockchain, fourthServer.getServer().getBlockchain());
    }

    /**
     * Unit test: 2 consensus instances, 1 client, 3 correct servers, 1 faulty
     * server
     * should decide the same value
     */
    @Test
    public void twoConsensusInstancesOneFaulty() {
        System.out.println("::::::::::::::::::::::::::::::::::::::::: TEST 4 :::::::::::::::::::::::::::::::::::::::::::::::");
        this.setupKeys();

        Blockchain blockchain = new Blockchain();

        final TransactionMessage t1 = new TransactionMessage(client2PublicKey, client1PublicKey, 15.0f);
        final TransactionMessage t2 = new TransactionMessage(client1PublicKey, client2PublicKey, 30.0f);
        final TransactionMessage t3 = new TransactionMessage(client2PublicKey, client1PublicKey, 25.0f);
        final TransactionMessage t4 = new TransactionMessage(client1PublicKey, client2PublicKey, 5.0f);

        final TransactionBlock block1 = new TransactionBlock("TRANSACTIONS");
        final TransactionBlock block2 = new TransactionBlock("TRANSACTIONS");
        block1.addTransaction(t1);
        block1.addTransaction(t2);
        block2.addTransaction(t3);
        block2.addTransaction(t4);
        blockchain.addTransactionBlock(block1);
        blockchain.addTransactionBlock(block2);

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 8446, "NF", "N", 1, "../configs/config4_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 8447, "NF", "N", 1, "../configs/config4_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 8448, "F", "N", 1, "../configs/config4_4.txt");
        App leader = new App(1, "127.0.0.1", 8445, "NF", "Y", 1, "../configs/config4_4.txt");
        AppClient client1 = new AppClient(1, "../configs/config4_4.txt", "127.0.0.1", 9561, 1);
        AppClient client2 = new AppClient(2, "../configs/config4_4.txt", "127.0.0.1", 9562, 1);


        client1.createAccount(client1PublicKey);
        client2.createAccount(client2PublicKey);

        client2.submitTransaction(client2PublicKey, client1PublicKey, 15.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 30.0f);

        client2.submitTransaction(client2PublicKey, client1PublicKey, 25.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 5.0f);

        // Wait for the value2 to be decided
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(blockchain, leader.getServer().getBlockchain());
        assertEquals(blockchain, secondServer.getServer().getBlockchain());
        assertEquals(blockchain, thirdServer.getServer().getBlockchain());
        assertEquals(blockchain, fourthServer.getServer().getBlockchain());
    }

    /**
     * 
     * Unit test: test correcteness of weak reads and strong reads
     */
    @Test
    public void testWeakReads() {
        System.out.println("::::::::::::::::::::::::::::::::::::::::: TEST 5 :::::::::::::::::::::::::::::::::::::::::::::::");
        this.setupKeys();

        float expectedBalance = 138.5f;
        float expectedStrong = 116.5f;
        float delta = 0.0001f;

        // Create 4 servers
        new App(2, "127.0.0.1", 9446, "NF", "N", 1, "../configs/config5_4.txt");
        new App(3, "127.0.0.1", 9447, "NF", "N", 1, "../configs/config5_4.txt");
        new App(4, "127.0.0.1", 9448, "F", "N", 1, "../configs/config5_4.txt");
        new App(1, "127.0.0.1", 9445, "NF", "Y", 1, "../configs/config5_4.txt");
        AppClient client1 = new AppClient(1, "../configs/config5_4.txt", "127.0.0.1", 9563, 1);
        AppClient client2 = new AppClient(2, "../configs/config5_4.txt", "127.0.0.1", 9564, 1);

        client1.createAccount(client1PublicKey);
        client2.createAccount(client2PublicKey);

        //block 1
        client2.submitTransaction(client2PublicKey, client1PublicKey, 4.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 2.0f);
        //block 2
        client2.submitTransaction(client2PublicKey, client1PublicKey, 6.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 8.0f);
        //block 3
        client2.submitTransaction(client2PublicKey, client1PublicKey, 20.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 10.0f);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client2.performWeakRead(client2PublicKey,2);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float balance1 = client2.getLastWeakRead();
        assertEquals(expectedBalance, balance1, delta);

        //block 3
        client2.submitTransaction(client2PublicKey, client1PublicKey, 40.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 20.0f);

        client2.performWeakRead(client2PublicKey, 2);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float balance2 = client2.getLastWeakRead();
        assertEquals(balance2, expectedBalance, delta);

        client2.performStrongRead(client2PublicKey);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float balance3 = client2.getLastStrongRead();
        assertEquals(balance3, expectedStrong, delta);
    }

    /**
     * Unit test: test that a forged signature is not accepted by the system
     */
    @Test
    public void testWeakReadsForgedSignature() {
        System.out.println("::::::::::::::::::::::::::::::::::::::::: TEST 6 :::::::::::::::::::::::::::::::::::::::::::::::");
        this.setupKeys();

        float expectedBalance = -2.0f;
        float delta = 0.0001f;

        // Create 4 servers
        new App(2, "127.0.0.1", 10446, "NF", "N", 1, "../configs/config6_4.txt");
        new App(3, "127.0.0.1", 10447, "NF", "N", 1, "../configs/config6_4.txt");
        new App(4, "127.0.0.1", 10448, "F", "N", 1, "../configs/config6_4.txt");
        new App(1, "127.0.0.1", 10445, "NF", "Y", 1, "../configs/config6_4.txt");
        AppClient client1 = new AppClient(1, "../configs/config6_4.txt", "127.0.0.1", 9565, 1);
        AppClient client2 = new AppClient(2, "../configs/config6_4.txt", "127.0.0.1", 9566, 1);

        client1.createAccount(client1PublicKey);
        client2.createAccount(client2PublicKey);

        //block 1
        client2.submitTransaction(client2PublicKey, client1PublicKey, 4.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 2.0f);
        //block 2
        client2.submitTransaction(client2PublicKey, client1PublicKey, 6.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 8.0f);
        //block 3
        client2.submitTransaction(client2PublicKey, client1PublicKey, 20.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 10.0f);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client2.performWeakRead(client2PublicKey,4);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float balance1 = client2.getLastWeakRead();
        assertEquals(expectedBalance, balance1, delta);
    }

    private byte[] readFile(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }

    public PublicKey readPublicKey(String publicKeyPath) throws Exception {
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        PublicKey pub = keyFacPub.generatePublic(pubSpec);
        return pub;
    }
}
