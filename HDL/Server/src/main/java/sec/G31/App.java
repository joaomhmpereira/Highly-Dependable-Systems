package sec.G31;

import java.io.*;
import java.util.Scanner;
import java.net.*;
import java.util.logging.Logger;

public class App
{
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    private Server _server;

    /**
     * Constructor for the App class (mainly to use in the tests)
     */
    public App(int serverId, String serverAddress, int serverPort, String faultType, String leaderFlag, int numF, String configFile, String messageToSend)
    {
        _server = initServer(serverId, serverAddress, serverPort, faultType, leaderFlag, numF);
        try {
            App.readFromFile(configFile, _server, leaderFlag, messageToSend);
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
            readFromFile(args[6], server, leaderFlag, "OLA");
            // while(true)
            //     readFromFile("System.in", server, leaderFlag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFromFile(String file, Server server, String leaderFlag, String messageToSend) throws IOException{
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

            LOGGER.info("Starting IBFT");
            server.startIBFT(messageToSend);
        
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static Server initServer(int serverId, String address, int port, String faultType, String leaderFlag, int numF){
        try {
            return new Server(serverId, InetAddress.getByName(address), port, faultType, leaderFlag, numF);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public Server getServer() {
        return _server;
    }
}
