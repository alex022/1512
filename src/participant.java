import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class participant {
	
	private final int VOTE_REQUEST = 1; 
	private final int GLOBAL_ABORT = 2;
	private final int GLOBAL_COMMIT = 3;
	private final int COMMIT = 4;
	private final int VOTE_COMMIT = 5; 
	private final int START_2PC = 6;
	private final int INIT = 7;
	private final int VOTE_ABORT = 8;
	
	private final int TIMEOUT = 20000;
	
	private Socket coordSocket;
	Thread participantThread;
	
	private PrintWriter cOut; 
	private BufferedReader cIn;
	Scanner scanner = new Scanner(System.in); 
	
	private int vote;
	private int localLog;
	
	private long time;
	
	public participant(int node)
	{	
		localLog= INIT;
		
		Thread participantThread = new Thread(new participantThread(node)); 
		participantThread.start(); 
	}
	
	class participantThread implements Runnable{

		private Socket coordSocket;
		PrintWriter cout;
		InputStream cin;
		
		int vote;
		
		public participantThread(int node)
		{
			try {
				coordSocket = new Socket("lisa.cs.ucsb.edu", (5000 + node));
				System.out.println("Participant " + node + " opened socket with coordinator"); 
				
				cout = new PrintWriter(coordSocket.getOutputStream());
				cin = coordSocket.getInputStream();
				System.out.println("Initialized input/output streams for participant " + node); 
			} catch (Exception e) {
				System.out.println("Participant " + node + "failed to open socket :("); 
			}
			
		}
		
		@Override
		public void run() {
			//while(true)
			{			
				try {
					while((vote = cin.read()) == -1);
					System.out.println("vote is " + vote); 
				} catch (Exception e) {
					System.out.println("Failed to read vote :("); 
				} 
			}
			
		}		
	}
	
		
	public void participate()
	{		
		time = System.currentTimeMillis();
		
		while(true)
		{		
			if((System.currentTimeMillis() - time) < TIMEOUT)
			{
				System.out.println("Entering participate() function"); 
				try {					
					while((vote = cIn.read()) != VOTE_REQUEST)
					{
						//System.out.println("Participant received " + Integer.toString(vote)); 
						if(vote == VOTE_REQUEST)
						{
							System.out.println("Participant received VOTE_REQUEST"); 
							break;
						}
					}
					System.out.println("Participant received " + Integer.toString(vote));
				} catch (IOException e) {
					System.out.println("Participant failed to read vote from coordinator");
				}
			}
			else
			{
				System.out.println("Participant has timed out"); 
				localLog = VOTE_ABORT;
				break; 
			}			
		}
		
		if(vote == VOTE_REQUEST)
		{
			System.out.print("Press 1 to vote commit or 2 to vote abort: ");
			vote = scanner.nextInt();
			
			if(vote == 1)
			{
				System.out.println("Writing VOTE_COMMIT to local log"); 
				localLog = VOTE_COMMIT;
				cOut.write(VOTE_COMMIT);
				
			}
			else
			{
				System.out.println("Writing VOTE_ABORT to local log"); 
				cOut.write(VOTE_ABORT);
			}
		}
	}
	
	
	

}
