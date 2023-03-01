package sec.G31;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner;


public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        // reading from the configuration file
        readFromFile("fileConfig.txt");
    }

    public static void readFromFile(String configFile){
        try {
            File myObj = new File(configFile);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String delimSpace = " ";
                String[] arr  = data.split(delimSpace);
                for (String uniqVal1 : arr1) {
                    System.out.println(uniqVal1);
                }
                InetAddress serverAddress = InetAddress.getByName(arr[0]);
                int serverPort = Integer.parseInt(arr[1]);  
                Server server = new Server(serverAddress, serverPort); // server creation
                System.out.println(data);
            }
            myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
}
