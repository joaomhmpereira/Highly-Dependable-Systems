package sec.G31;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Unit test: 4 correct servers should decide the same value
     */
    @Test
    public void fourCorrectServers() {
        final String valueToDecide = "ALL CORRECT";

        // Create 4 servers
        App secondServer = new App(2, "127.0.0.1", 4446, "NF", "N", 1, "configs/config1_4.txt", valueToDecide);
        App thirdServer = new App(3, "127.0.0.1", 4447, "NF", "N", 1, "configs/config1_4.txt", valueToDecide);
        App fourthServer = new App(4, "127.0.0.1", 4448, "NF", "N", 1, "configs/config1_4.txt", valueToDecide);
        App leader = new App(1, "127.0.0.1", 4445, "NF", "Y", 1, "configs/config1_4.txt", valueToDecide);

        // Wait for the value to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the value was decided and it is the same for all servers
        assertEquals(valueToDecide, leader.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, secondServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, thirdServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, fourthServer.getServer().getIBFT().getDecidedValue());
    }

    /**
     * Unit test: 3 correct servers and 1 faulty server should decide the same value
     */
    @Test
    public void threeCorrectOneFaulty() {
        final String valueToDecide = "ONE FAULTY";

        // Create 4 servers (server with id 4 is faulty)
        App secondServer = new App(2, "127.0.0.1", 5446, "NF", "N", 1, "configs/config2_4.txt", valueToDecide);
        App thirdServer = new App(3, "127.0.0.1", 5447, "NF", "N", 1, "configs/config2_4.txt", valueToDecide);
        App fourthServer = new App(4, "127.0.0.1", 5448, "F", "N", 1, "configs/config2_4.txt", valueToDecide);
        App leader = new App(1, "127.0.0.1", 5445, "NF", "Y", 1, "configs/config2_4.txt", valueToDecide);

        // Wait for the value to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the value was decided and it is the same for all servers
        assertEquals(valueToDecide, leader.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, secondServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, thirdServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, fourthServer.getServer().getIBFT().getDecidedValue());
    }

    /**
     * Unit test: 5 correct servers and 2 faulty servers should decide the same
     * value
     */
    @Test
    public void fiveCorrectTwoFaulty() {
        final String valueToDecide = "SEVEN SERVERS, TWO ARE FAULTY";

        // Create 7 servers (server with id 4 and 5 are faulty)
        App secondServer = new App(2, "127.0.0.1", 6446, "NF", "N", 2, "configs/config1_7.txt", valueToDecide);
        App thirdServer = new App(3, "127.0.0.1", 6447, "NF", "N", 2, "configs/config1_7.txt", valueToDecide);
        App fourthServer = new App(4, "127.0.0.1", 6448, "F", "N", 2, "configs/config1_7.txt", valueToDecide);
        App fifthServer = new App(5, "127.0.0.1", 6449, "F", "N", 2, "configs/config1_7.txt", valueToDecide);
        App sixthServer = new App(6, "127.0.0.1", 6450, "NF", "N", 2, "configs/config1_7.txt", valueToDecide);
        App seventhServer = new App(7, "127.0.0.1", 6451, "NF", "N", 2, "configs/config1_7.txt", valueToDecide);
        App leader = new App(1, "127.0.0.1", 6445, "NF", "Y", 2, "configs/config1_7.txt", valueToDecide);

        // Wait for the value to be decided
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the value was decided and it is the same for all servers
        assertEquals(valueToDecide, leader.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, secondServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, thirdServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, fourthServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, fifthServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, sixthServer.getServer().getIBFT().getDecidedValue());
        assertEquals(valueToDecide, seventhServer.getServer().getIBFT().getDecidedValue());
    }
}
