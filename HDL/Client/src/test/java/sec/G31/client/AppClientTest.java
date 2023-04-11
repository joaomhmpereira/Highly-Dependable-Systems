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
     * Unit test: 5 correct servers and 2 faulty servers should decide the same
     * value
     */
    @Test
    public void fiveCorrectTwoFaulty() {
        final String valueToDecide = "SEVEN SERVERS, TWO ARE FAULTY";

        // Create 7 servers (server with id 4 and 5 are faulty)
        App secondServer = new App(2, "127.0.0.1", 6446, "NF", "N", 2, "../configs/config1_7.txt");
        App thirdServer = new App(3, "127.0.0.1", 6447, "NF", "N", 2, "../configs/config1_7.txt");
        App fourthServer = new App(4, "127.0.0.1", 6448, "F", "N", 2, "../configs/config1_7.txt");
        App fifthServer = new App(5, "127.0.0.1", 6449, "F", "N", 2, "../configs/config1_7.txt");
        App sixthServer = new App(6, "127.0.0.1", 6450, "NF", "N", 2, "../configs/config1_7.txt");
        App seventhServer = new App(7, "127.0.0.1", 6451, "NF", "N", 2, "../configs/config1_7.txt");
        App leader = new App(1, "127.0.0.1", 6445, "NF", "Y", 2, "../configs/config1_7.txt");
        AppClient client = new AppClient(1, "../configs/config1_7.txt", "127.0.0.1", 9557, 2);

        // client.submitValue(valueToDecide);

        // Wait for the value to be decided
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the value was decided and it is the same for all servers
        assertEquals(valueToDecide, leader.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, secondServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, thirdServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, fourthServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, fifthServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, sixthServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, seventhServer.getServer().getLastDecidedValue());
    }

    /**
     * Unit test: 2 consensus instances with 4 correct servers
     */
    @Test
    public void twoConsensusInstancesAllCorrect() {
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

        //try {
        //    Thread.sleep(200);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}

        client2.submitTransaction(client2PublicKey, client1PublicKey, 15.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 30.0f);

        client2.submitTransaction(client2PublicKey, client1PublicKey, 25.0f);
        client1.submitTransaction(client1PublicKey, client2PublicKey, 5.0f);

        // Wait for the value2 to be decided
        try {
            Thread.sleep(1500);
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
        final String finalBlockchainValue = "Instance1OneFaulty.Instance2OneFaulty.";
        final String valueToDecide1 = "Instance1OneFaulty";
        final String valueToDecide2 = "Instance2OneFaulty";

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 8446, "NF", "N", 1, "../configs/config4_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 8447, "NF", "N", 1, "../configs/config4_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 8448, "F", "N", 1, "../configs/config4_4.txt");
        App leader = new App(1, "127.0.0.1", 8445, "NF", "Y", 1, "../configs/config4_4.txt");
        AppClient client = new AppClient(1, "../configs/config4_4.txt", "127.0.0.1", 9559, 1);

        // client.submitValue(valueToDecide1);
        // client.submitValue(valueToDecide2);

        // Wait for the value2 to be decided
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(finalBlockchainValue, leader.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, secondServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, thirdServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, fourthServer.getServer().getBlockchainString());
    }

    /**
     * Unit test: 2 consensus instances, 2 clients, 3 correct servers, 1 faulty
     * server
     * should decide the same value
     */
    @Test
    public void twoConsensusInstancestwoClientsOneFaultyServer() {
        final String finalBlockchainValue = "Message1.Message2.";
        final String valueToDecide1 = "Message1";
        final String valueToDecide2 = "Message2";

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 9446, "NF", "N", 1, "../configs/config5_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 9447, "NF", "N", 1, "../configs/config5_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 9448, "F", "N", 1, "../configs/config5_4.txt");
        App leader = new App(1, "127.0.0.1", 9445, "NF", "Y", 1, "../configs/config5_4.txt");
        AppClient client1 = new AppClient(1, "../configs/config5_4.txt", "127.0.0.1", 9560, 1);
        AppClient client2 = new AppClient(2, "../configs/config5_4.txt", "127.0.0.1", 9561, 1);

        // client1.submitValue(valueToDecide1);
        // client2.submitValue(valueToDecide2);

        // Wait for the value2 to be decided
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(finalBlockchainValue, leader.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, secondServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, thirdServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, fourthServer.getServer().getBlockchainString());
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
