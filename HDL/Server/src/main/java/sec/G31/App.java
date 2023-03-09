package sec.G31;

import java.io.*;
import java.util.Scanner;
import java.net.*;
import java.util.logging.Logger;
import org.apache.commons.lang3.*;
import sec.G31.messages.Message;

public class App
{
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args)
    {
        System.out.println("Hello World!");
        /**
         * Input received in the form of:
         * serverId, serverAddress, serverPort, faultType, leader (?) 
         */

        // Check if the number of arguments is correct
        if (args.length != 6) {
            System.out.println("Usage: java App <serverId> <serverAddress> <serverPort> <faultType> <leaderFlag> <configFile>");
            System.exit(1);
        }

        final int serverId = Integer.parseInt(args[0]);
        final String serverAddress = args[1];
        // Convert port from String to int
		final int serverPort = Integer.parseInt(args[2]);
        final String faultType = args[3];
        final String leaderFlag = args[4];
        final int numF = 1;

        Server server = initServer(serverId, serverAddress, serverPort, faultType, leaderFlag, numF);
        LOGGER.info("Server created: " + server.toString());
        // reading from the configuration file
        try {
            readFromFile(args[5], server, leaderFlag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFromFile(String configFile, Server server, String leaderFlag) throws IOException{
        try {
            File myObj = new File(configFile);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String delimSpace = " ";
                String[] arr  = data.split(delimSpace);
                //if (server.getId() != Integer.parseInt(arr[0]))
                server.newNeighbor(Integer.parseInt(arr[0]), Integer.parseInt(arr[2])); // include our own server in the hashtable
                //for (String str : arr) {
                //    LOGGER.info(str);
                //}
            }
            myReader.close();
            
            if (leaderFlag.equals("Y")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter the value to be sent: ");
                String msg = reader.readLine();
                server.startIBFT(msg);                
            }

            // trying to send a message
            //String destAddr = "127.0.0.1";
            //int destPort = 4446;
            //Message prepareMessage = new Message("PREPARE", 1, "Ola");
            //byte[] data = SerializationUtils.serialize(prepareMessage);
            
            
            //while(!msg.equals("end")){
            //    // Reading data using readLine
            //    try {
            //        // Enter data using BufferReader
            //        msg = reader.readLine();
            //    } catch (IOException e) {
            //        e.printStackTrace();
            //    }
            //    server.sendMessage(destAddr, destPort, msg);
            //}
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
}
