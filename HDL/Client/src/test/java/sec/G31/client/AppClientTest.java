package sec.G31.client;

import static org.junit.Assert.*;

import sec.G31.*;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppClientTest 
{

    @Test
    public void fourCorrectServers(){
        final String valueToDecide = "ALL CORRECT";

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 4446, "NF", "N", 1, "../configs/config1_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 4447, "NF", "N", 1, "../configs/config1_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 4448, "NF", "N", 1, "../configs/config1_4.txt");
        App leader = new App(1, "127.0.0.1", 4445, "NF", "Y", 1, "../configs/config1_4.txt");
        AppClient client = new AppClient(1, "../configs/config1_4.txt", "127.0.0.1", 9555);

        client.submitValue(valueToDecide);

        // Wait for the value to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the value was decided and it is the same for all servers
        assertEquals(valueToDecide, leader.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, secondServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, thirdServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, fourthServer.getServer().getLastDecidedValue());
    }

    @Test
    public void threeCorrectOneFaulty() {
        final String valueToDecide = "ONE FAULTY";

        // Create 4 servers (server with id 4 is faulty)
        App leader = new App(1, "127.0.0.1", 5445, "NF", "Y", 1, "../configs/config2_4.txt");
        App secondServer = new App(2, "127.0.0.1", 5446, "NF", "N", 1, "../configs/config2_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 5447, "NF", "N", 1, "../configs/config2_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 5448, "F", "N", 1, "../configs/config2_4.txt");
        AppClient client = new AppClient(1, "../configs/config2_4.txt", "127.0.0.1", 9556);

        client.submitValue(valueToDecide);

        // Wait for the value to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the value was decided and it is the same for all servers
        assertEquals(valueToDecide, leader.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, secondServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, thirdServer.getServer().getLastDecidedValue());
        assertEquals(valueToDecide, fourthServer.getServer().getLastDecidedValue());
    }

    /**
     * Unit test: 5 correct servers and 2 faulty servers should decide the same value
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
        AppClient client = new AppClient(1, "../configs/config1_7.txt", "127.0.0.1", 9557);

        client.submitValue(valueToDecide);

        // Wait for the value to be decided
        try {
            Thread.sleep(500);
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
    public void twoConsensusInstancesAllCorrect(){
        final String finalBlockchainValue = "Instance1.Instance2.";
        final String valueToDecide1 = "Instance1";
        final String valueToDecide2 = "Instance2";

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 7446, "NF", "N", 1, "../configs/config3_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 7447, "NF", "N", 1, "../configs/config3_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 7448, "NF", "N", 1, "../configs/config3_4.txt");
        App leader = new App(1, "127.0.0.1", 7445, "NF", "Y", 1, "../configs/config3_4.txt");
        AppClient client = new AppClient(1, "../configs/config3_4.txt", "127.0.0.1", 9558);

        client.submitValue(valueToDecide1);

        // Wait for the value1 to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.submitValue(valueToDecide2);

        // Wait for the value2 to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(finalBlockchainValue, leader.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, secondServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, thirdServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, fourthServer.getServer().getBlockchainString());
    }

    /**
     * Unit test: 2 consensus instances, 1 client, 3 correct servers, 1 faulty server
     * should decide the same value
     */
    @Test
    public void twoConsensusInstancesOneFaulty(){
        final String finalBlockchainValue = "Instance1OneFaulty.Instance2OneFaulty.";
        final String valueToDecide1 = "Instance1OneFaulty";
        final String valueToDecide2 = "Instance2OneFaulty";

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 8446, "NF", "N", 1, "../configs/config4_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 8447, "NF", "N", 1, "../configs/config4_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 8448, "F", "N", 1, "../configs/config4_4.txt");
        App leader = new App(1, "127.0.0.1", 8445, "NF", "Y", 1, "../configs/config4_4.txt");
        AppClient client = new AppClient(1, "../configs/config4_4.txt", "127.0.0.1", 9559);

        client.submitValue(valueToDecide1);

        // Wait for the value1 to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.submitValue(valueToDecide2);

        // Wait for the value2 to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(finalBlockchainValue, leader.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, secondServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, thirdServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, fourthServer.getServer().getBlockchainString());
    }

    /**
     * Unit test: 2 consensus instances, 2 clients, 3 correct servers, 1 faulty server
     * should decide the same value
     */
    @Test
    public void twoConsensusInstancestwoClientsOneFaultyServer(){
        final String finalBlockchainValue = "Message1.Message2.";
        final String valueToDecide1 = "Message1";
        final String valueToDecide2 = "Message2";

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 9446, "NF", "N", 1, "../configs/config5_4.txt");
        App thirdServer = new App(3, "127.0.0.1", 9447, "NF", "N", 1, "../configs/config5_4.txt");
        App fourthServer = new App(4, "127.0.0.1", 9448, "F", "N", 1, "../configs/config5_4.txt");
        App leader = new App(1, "127.0.0.1", 9445, "NF", "Y", 1, "../configs/config5_4.txt");
        AppClient client1 = new AppClient(1, "../configs/config5_4.txt", "127.0.0.1", 9560);
        AppClient client2 = new AppClient(2, "../configs/config5_4.txt", "127.0.0.1", 9561);


        client1.submitValue(valueToDecide1);

        // Wait for the value1 to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client2.submitValue(valueToDecide2);

        // Wait for the value2 to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(finalBlockchainValue, leader.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, secondServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, thirdServer.getServer().getBlockchainString());
        assertEquals(finalBlockchainValue, fourthServer.getServer().getBlockchainString());
    }
}
