package sec.G31;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner;
import java.io.*;
import java.net.*;

public class App 
{
    
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        // Convert port from String to int
		final int port = Integer.parseInt(args[0]);
        
        // reading from the configuration file
        readFromFile("fileConfig.txt", port);
    }

    public static void readFromFile(String configFile, int port){
        try {
            // file line: "ip address" "port address"
            File myObj = new File(configFile);
            Scanner myReader = new Scanner(myObj);
            Server myserver = new Server(InetAddress.getByName("127.0.0.1"), port); 
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String delimSpace = " ";
                String[] arr  = data.split(delimSpace);
                for (String uniqVal1 : arr) {
                    System.out.println(uniqVal1);
                }
                //InetAddress serverAddress = InetAddress.getByName(arr[0]);
                //int serverPort = Integer.parseInt(arr[1]);  
                //Server server = new Server(serverAddress, serverPort); // server creation
            }
            myReader.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // trying to send a message
            String destAddr = "127.0.0.1";
            int destPort = 4446;
            String msg = "";
            while(!msg.equals("end")){
                // Reading data using readLine
                try {
                    // Enter data using BufferReader
                    msg = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                myserver.sendMessage(destAddr, destPort, msg);
            }
        } catch (UnknownHostException e){
            System.out.println("No such host");
            e.printStackTrace();
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
