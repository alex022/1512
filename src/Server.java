import java.net.*;
import java.util.*;
import java.io.*;
 
public class Server
{
	String toClients = null;
	String fromClients = null;
	volatile static int reqCount = 0;
	volatile static int demoCount = 0;
    public static void main(String[] args)
    {
        new Server();
    }
     
    public Server()
    {
        //We need a try-catch because lots of errors can be thrown
        try {
            ServerSocket sSocket = new ServerSocket(6000);
            ServerSocket participant2 = new ServerSocket(6001);
            System.out.println("Server started at: " + new Date());
            System.out.println("Please input for each participant");
             
             
            //Loop that runs server functions
            //while(true) {
                //Wait for a client to connect
                Socket socket = sSocket.accept();
                ClientThread cT = new ClientThread(socket);
                new Thread(cT).start();
                Socket socket2 = participant2.accept();
                       
                //Create a new custom thread to handle the connection
                //ClientThread cT = new ClientThread(socket);
                ClientThread cT2 = new ClientThread(socket2);
                 
                //Start the thread!
//                new Thread(cT).start();
                new Thread(cT2).start();
            //}
        } catch(IOException exception) {
            System.out.println("Error: " + exception);
        }
    }
     
    //Here we create the ClientThread inner class and have it implement Runnable
    //This means that it can be used as a thread
    class ClientThread implements Runnable
    {
        Socket threadSocket;
        PrintWriter output;
        Scanner scanner = new Scanner(System.in);
         
        //This constructor will be passed the socket
        public ClientThread(Socket socket)
        {
            //Here we set the socket to a local variable so we can use it later
            threadSocket = socket;
            try {
				socket.setSoTimeout(8000);
			} catch (SocketException e1) {
				
			}
            try {
				output = new PrintWriter(threadSocket.getOutputStream(), true);
			} catch (IOException e) {}
        }
         
        public void run()
        {
            //All this should look familiar
            try {
                //Create the streams
                //PrintWriter output = new PrintWriter(threadSocket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(threadSocket.getInputStream()));
                 
                //Tell the client that he/she has connected
                output.println("You have connected at: " + new Date());
                 
                while (true) {
                	demoCount++;
                    //This will wait until a line of text has been sent
                    //String chatInput = input.readLine();
                    //System.out.println(chatInput);
                	System.out.println("1-VOTE_REQUEST/2-GLOBAL_COMMIT/3-GLOBAL_ABORT");
                	toClients = null;                		
                	toClients = scanner.nextLine();
                    output.println(toClients);
                    
                    //Check reply from participants
                    fromClients = input.readLine();
                    String message = null;
                    switch(fromClients){
                    case "1": message = "VOTE_COMMIT";
                    		break;
                    case "2": message = "VOTE_ABORT";
                    		break;
                    }
                    System.out.println("Message from Participant: " + message);
                    
                    if(fromClients.equals("1"))
                    	reqCount++;
                    else if(fromClients.equals("1"))
                    	reqCount = 0;
                     
                    if(demoCount == 10){
                    	System.out.println("Sending GLOBAL_COMMIT to 1 participant only");
                    	toClients = scanner.nextLine();
                        output.println(toClients);
                    }
                    	
                    try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {}
                    
                    if(reqCount == 2)
                    {
                    	System.out.println("Sending GLOBAL_COMMIT");
                    	output.println("2");
                    }
                    else
                    {
                    	System.out.println("Sending GLOBAL_ABORT");
                    	output.println("3");
                    }
                    
                }
            } catch(Exception exception) {
            	System.out.println("Globally aborting. Please restart program");
            	output.println("3");
            	return;
            }
        }
    }
}