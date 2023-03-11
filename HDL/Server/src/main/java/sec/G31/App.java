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
            if(file.equals("fileConfig.txt")){
                File myObj = new File(file);
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

                LOGGER.info("Starting IBFT");
                server.startIBFT("START_MESSAGE");
            } 

            // nao funciona:
            // else if(file.equals("System.in")){
            //     Scanner myReader = new Scanner(System.in);
            //     System.out.println("New Message:");
            //     String message = ".";
            //     for (int i = 0; i < 1; i++){
            //         message = message + myReader.nextLine();
            //     }
            //     myReader.close();

            //     LOGGER.info("Starting new IBFT");
            //     server.startIBFT(message);
            // }



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
