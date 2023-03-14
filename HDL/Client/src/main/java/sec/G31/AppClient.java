package sec.G31;
// import SEC.HDS.Server.src.main.java.sec.G31.*;
// import SEC.HDL.Server.src.main.java.sec.G31.messages.Message;

import java.io.*;
import java.util.*;
import sec.G31.messages.InitInstance;

/**
 * Hello world!
 *
 */
public class AppClient 
{

    //private BroadcastManagerClient _broadcastManager;
    //private int _clientId;
//
    //public AppClient(int clientId, String configFile) {
    //    _clientId = clientId;
    //    try {
    //        Hashtable<Integer, Integer> servers = readFromFile(configFile);
    //        _broadcastManager = new BroadcastManagerClient(servers);
    //        
    //    } catch (IOException e) {
    //        e.printStackTrace();
    //    }
    //}

    public static Hashtable<Integer, Integer> readFromFile(String file) throws IOException{
        try {
            Hashtable<Integer, Integer> servers = new Hashtable<Integer, Integer>();
            File myObj = new File(file);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String delimSpace = " ";
                String[] arr  = data.split(delimSpace);
                servers.put(Integer.parseInt(arr[0]), Integer.parseInt(arr[2]));
            }
            myReader.close();

            return servers;
        
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    public static void main( String[] args )
    {
        // Check if the number of arguments is correct
        if (args.length != 2) {
            System.out.println("Usage: java App <clientId> <configFile>");
            System.exit(1);
        }
        
        int _clientId = Integer.parseInt(args[0]);
        final String config = args[1];
        Hashtable<Integer, Integer> _servers;
        //int _clientPort;
        System.out.println("=== Hello Client " + _clientId + " ===");

        try {
            _servers = readFromFile(config);
            BroadcastManagerClient _broadcastManager = new BroadcastManagerClient(_servers);
            Scanner inputScanner = new Scanner(System.in);
            System.out.println("Enter a new message (type \"QUIT\" to end server):");
            String newMessage = "";
            //System.out.println("You entered:" + newMessage);
            while(true){
                newMessage = inputScanner.nextLine();
                if (newMessage.equals("QUIT")) {
                    System.out.println("=== Goodbye Client " + _clientId + " ===");
                    break;
                }
            
            InitInstance msg = new InitInstance(_clientId, newMessage);
            // broadcast value to all servers
            _broadcastManager.sendBroadcast(msg);
        }
        
        inputScanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
