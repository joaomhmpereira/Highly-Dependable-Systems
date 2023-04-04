package sec.G31.client;

import java.io.*;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import sec.G31.messages.Message;
import sec.G31.messages.TransactionMessage;

/**
 * Hello world!
 *
 */
public class AppClient 
{
    private int _nonceCounter;
    private int _clientId;
    private int _port;
    private int _F;
    BroadcastManagerClient _broadcastManager;

    public AppClient(int clientId, String configFile, String address, int port, int numFaulties) {
        _nonceCounter = 0;
        _clientId = clientId;
        _port = port;
        _F = numFaulties;
        try {
            Hashtable<Integer, Integer> servers = readFromFile(configFile);
            InetAddress addr = InetAddress.getByName(address);
            _broadcastManager = new BroadcastManagerClient(addr, port, servers, clientId, _F);
            
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
        if (args.length != 5) {
            System.out.println("Usage: java App <clientId> <address> <port> <numFaulties> <configFile>");
            System.exit(1);
        }
        
        int _nonceCounter = 0;
        int _clientId = Integer.parseInt(args[0]);
        final String _address = args[1];
        final int _port = Integer.parseInt(args[2]);
        final int _F = Integer.parseInt(args[3]);
        final String config = args[4];
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
            BroadcastManagerClient _broadcastManager = new BroadcastManagerClient(addr, _port, _servers, _clientId, _F);
            
            Scanner inputScanner = new Scanner(System.in);
            System.out.print("Available commands:\n [1] CREATE - to create an new account.\n [2] BALANCE - to check an accounts balance\n [3] TRANSFER - to make a transfer.\n [4] QUIT - to quit.\nPlease enter the number of the command: ");
            String newMessage = "";
            //System.out.println("You entered:" + newMessage);
            while(true){
                newMessage = inputScanner.nextLine();
                if (newMessage.equals("4")) {
                    System.out.println("=== Goodbye Client " + _clientId + " ===");
                    break;
                }
                
                /**
                 *  TRANSFER 
                 */
                else if (newMessage.equals("3")){
                    System.out.print("Enter the source public key: ");
                    String sourcePath = inputScanner.nextLine();
                    PublicKey source = readPublicKey(sourcePath);
                    System.out.print("Enter the destination public key: ");
                    String destinationPath = inputScanner.nextLine();
                    PublicKey destination = readPublicKey(destinationPath);
                    System.out.print("Enter the amount: ");
                    int amount = Integer.parseInt(inputScanner.nextLine());
                    TransactionMessage transaction = new TransactionMessage(source, destination, amount);
                    Message msg = new Message("TRANSACTION", transaction, _clientId, _port, _nonceCounter);
                    _broadcastManager.sendBroadcast(msg);
                } 
                /**
                 * CHECK BALANCE
                 */
                else if (newMessage.equals("2")){
                    System.out.print("Enter the public key: ");
                    String publicKeyPath = inputScanner.nextLine();
                    PublicKey publicKey = readPublicKey(publicKeyPath);
                    Message msg = new Message("BALANCE", _clientId, _port, _nonceCounter, publicKey);
                    _broadcastManager.sendBroadcast(msg);
                }

                /**
                 * CREATE ACCOUNT
                 */
                else if (newMessage.equals("1")){
                    System.out.print("Enter the public key: ");
                    String publicKeyPath = inputScanner.nextLine();
                    PublicKey publicKey = readPublicKey(publicKeyPath);
                    Message msg = new Message("CREATE", _clientId, _port, _nonceCounter, publicKey);
                    _broadcastManager.sendBroadcast(msg);
                }
                _nonceCounter++;
            }
            inputScanner.close();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void submitValue(PublicKey source, PublicKey destination, int amout){
        TransactionMessage transaction = new TransactionMessage(source, destination, amout);
        Message msg = new Message("START", transaction, _clientId, _port, _nonceCounter);
        _broadcastManager.sendBroadcast(msg);
        _nonceCounter++;
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
