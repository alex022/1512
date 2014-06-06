import java.io.*;
import java.net.*;
//We need a Scanner to receive input from the user
import java.util.Scanner;
 
public class Client
{
	String toServer = null;
	String fromServer = null;
	volatile static int messageCount = 0;
    public static void main(String[] args)
    {
    	int nodeNum = Integer.parseInt(args[0]); 
        new Client(nodeNum);
    }
     
    public Client(int node)
    {
    //We set up the scanner to receive user input
        Scanner scanner = new Scanner(System.in);
        try {
            Socket socket = new Socket("optimus.cs.ucsb.edu",6000+node);
            socket.setSoTimeout(15000);
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             
            //This will wait for the server to send the string to the client saying a connection
            //has been made.
            fromServer = input.readLine();
            System.out.println(fromServer);
            //Again, here is the code that will run the client, this will continue looking for 
            //input from the user then it will send that info to the server.
            while(true) {
                fromServer = null;
                fromServer = input.readLine();
                System.out.println(fromServer);
                String message = null;
                switch(fromServer){
                case "1": message = "VOTE_REQUEST";
                		break;
                case "2": message = "GLOBAL_COMMIT";
                		break;
                case "3": message = "GLOBAL_ABORT";
                		break;
                }
                
                System.out.println("Message from Coordinator: " + message);
                //If coordinator VOTE_REQUESTS
                if(message.equals("VOTE_REQUEST")){
                	System.out.println("Reply back with 1-VOTE_COMMIT/2-VOTE_ABORT");
                	toServer = scanner.nextLine();
                	output.println(toServer);
                }
                else if(message.equals("GLOBAL_COMMIT")){
                	messageCount++;
                	System.out.println("Locally committing");
                }
                else if(message.equals("GLOBAL_ABORT"))
                	System.out.println("Locally aborting"); 
                messageCount = 0;
            }
        } catch (Exception exception) {
        	if(messageCount == 1){
        		if(node == 0){
        			System.out.println("Receiving GLOBAL_COMMIT from Client 1");
        			System.out.println("Locally committing");
        		}
        		else if(node == 1){
        			System.out.println("Receiving GLOBAL_COMMIT from Client 0");
        			System.out.println("Locally committing");
        		}
        		messageCount = 0;
        		return;
        	}
            System.out.println("No expected message from Coordinator. Locally aborting");
            return;
        }
    }
}