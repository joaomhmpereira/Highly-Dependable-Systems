package sec.G31;
// import SEC.HDS.Server.src.main.java.sec.G31.*;
// import SEC.HDL.Server.src.main.java.sec.G31.messages.Message;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // Check if the number of arguments is correct
        if (args.length != 1) {
            System.out.println("Usage: java App <clientId>");
            System.exit(1);
        }

        int _clientId = Integer.parseInt(args[0]);
        int _clientPort;

        
        System.out.println("===Hello Client " + _clientId + " ===");

        // LOGGER.info("Server created: " + server.toString());

        Scanner inputScanner = new Scanner(System.in);
        System.out.print("Enter a new message:");
        String newMessage = inputScanner.nextLine();
        System.out.println("You entered:" + newMessage);
        
        inputScanner.close();
    }
}
