package sec.G31.client;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import sec.G31.messages.Message;

/**
 * Hello world!
 *
 */
public class AppClient 
{
    private int _nonceCounter;
    private int _clientId;
    private int _port;
    BroadcastManagerClient _broadcastManager;

    public AppClient(int clientId, String configFile, String address, int port) {
        _nonceCounter = 0;
        _clientId = clientId;
        _port = port;
        try {
            Hashtable<Integer, Integer> servers = readFromFile(configFile);
            InetAddress addr = InetAddress.getByName(address);
            _broadcastManager = new BroadcastManagerClient(addr, port, servers, clientId);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        if (args.length != 4) {
            System.out.println("Usage: java App <clientId> <address> <port> <configFile>");
            System.exit(1);
        }
        
        int _nonceCounter = 0;
        int _clientId = Integer.parseInt(args[0]);
        final String _address = args[1];
        final int _port = Integer.parseInt(args[2]);
        final String config = args[3];
        Hashtable<Integer, Integer> _servers;
        //int _clientPort;
        System.out.println("=== Hello Client " + _clientId + " ===");

        try {
            _servers = readFromFile(config);

            //print servers
            for(int i = 1; i <= _servers.size(); i++){
                System.out.println("Server " + i + " is listening on port " + _servers.get(i));
            }

            InetAddress addr = InetAddress.getByName(_address);
            BroadcastManagerClient _broadcastManager = new BroadcastManagerClient(addr, _port, _servers, _clientId);
            
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
                //InitInstance msg = new InitInstance(_clientId, newMessage);
                Message msg = new Message("START", newMessage, _clientId, _port, _nonceCounter);
                // broadcast value to all servers
                _broadcastManager.sendBroadcast(msg);
                // to diferentiate the next message even if the content is equal
                _nonceCounter++;
            }
            inputScanner.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void submitValue(String value){
        Message msg = new Message("START", value, _clientId, _port, _nonceCounter);
        _broadcastManager.sendBroadcast(msg);
        _nonceCounter++;
    }
}
