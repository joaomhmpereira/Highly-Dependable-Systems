package sec.G31;

import java.io.*;
import java.util.Scanner;
import java.net.*;
import java.util.logging.Logger;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class App
{
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    private Server _server;

    /**
     * Constructor for the App class (mainly to use in the tests)
     */
    public App(int serverId, String serverAddress, int serverPort, String faultType, String leaderFlag, int numF, String configFile)
    {
        _server = initServer(serverId, serverAddress, serverPort, faultType, leaderFlag, numF);
        try {
            App.readFromFile(configFile, _server, leaderFlag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {   
        // Check if the number of arguments is correct
        if (args.length != 7) {
            System.out.println("Usage: java App <serverId> <serverAddress> <serverPort> <faultType> <leaderFlag> <numFaulties> <configFile>");
            System.exit(1);
        }

        final int serverId = Integer.parseInt(args[0]);
        final String serverAddress = args[1];
        // Convert port from String to int
		final int serverPort = Integer.parseInt(args[2]);
        final String faultType = args[3];
        final String leaderFlag = args[4];
        final int numF = Integer.parseInt(args[5]); // ou podemos mandar um ficheiro com o num la dentro, depois decidam

        Server server = initServer(serverId, serverAddress, serverPort, faultType, leaderFlag, numF);
        LOGGER.info("Server created: " + server.toString());
        // reading from the configuration file
        try {
            readFromFile(args[6], server, leaderFlag);
            // while(true)
            //     readFromFile("System.in", server, leaderFlag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFromFile(String file, Server server, String leaderFlag) throws IOException{
        try {
            File myObj = new File(file);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String delimSpace = " ";
                String[] arr  = data.split(delimSpace);
                server.newNeighbor(Integer.parseInt(arr[0]), Integer.parseInt(arr[2])); // include our own server in the hashtable
            }
            myReader.close();

            //LOGGER.info("Starting IBFT");
            //server.startIBFT(messageToSend);
        
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    // private static PublicKey getLeaderPubKey() {
    //     try {
    //         String publicKeyPath = "../keys/1/public_key.der"; //assuming leader is always 1
    //         return readPublicKey(publicKeyPath);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         System.exit(1);
    //     }
    //     return null;
    // }

    public static Server initServer(int serverId, String address, int port, String faultType, String leaderFlag, int numF){
        try {
            // PublicKey leaderPubKey = getLeaderPubKey();
            // if (leaderPubKey == null) {
            //     return null;
            // }
            String publicKeyPath = "../keys/1/public_key.der"; //assuming leader is always 1
            PublicKey leaderPubKey = readPublicKey(publicKeyPath);
            return new Server(serverId, InetAddress.getByName(address), port, faultType, leaderFlag, numF, leaderPubKey);
        } catch (/*UnknownHostException || */Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public Server getServer() {
        return _server;
    }

    private static byte[] readFile(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }

    public static PublicKey readPublicKey(String publicKeyPath) throws Exception {
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        PublicKey pub = keyFacPub.generatePublic(pubSpec);
        return pub;
    }
}
