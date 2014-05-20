import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class main {
	
	private static final int VOTE_REQUEST = 1; 
	private static final int GLOBAL_ABORT = 2;
	private static final int GLOBAL_COMMIT = 3;
	private static final int COMMIT = 4;
	private static final int VOTE_COMMIT = 5; 
	private static final int START_2PC = 6; 
	
	private static Socket fParticipant;	
	private static Socket sParticipant;
	
	private static PrintWriter fOut;
	private static PrintWriter sOut; 
	private static DataInputStream fIn;
	private static DataInputStream sIn;
	
	
	private static int votes[];
	private static int localLog;
	
	private static long currentTime;
	
	public static void main(String args[]) throws UnknownHostException, IOException
	{	
		localLog= START_2PC;
		votes = new int[2];
		votes[0] = 0; votes[1] = 0;
		localLog = 0;
		
		//Initialize sockets, writers, and readers
		try{
			fParticipant = new Socket("cartman.cs.ucsb.edu", 5000);
			sParticipant = new Socket("bart.cs.ucsb.edu", 50001);
			
			fOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fParticipant.getOutputStream())), true);
			sOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sParticipant.getOutputStream())), true);
			
			fIn = new DataInputStream(fParticipant.getInputStream());
			sIn = new DataInputStream(sParticipant.getInputStream());
		} catch(Exception e)
		{
			System.out.println("Failed to open socket :("); 
		}
		
		while(localLog == START_2PC)
		{
			//Multicast vote request to all participants
			fOut.write(VOTE_REQUEST);
			sOut.write(VOTE_REQUEST);
			
			currentTime = System.currentTimeMillis(); 
			
			//Wait for all votes to be collected
			while(votes[0] == 0 && votes[1] == 0)
			{	
				votes[0] = fIn.read(); 
				
				//If loop times out, record and broadcast global abort and exit
				if((System.currentTimeMillis() - currentTime) > 10000)
				{
					localLog = GLOBAL_ABORT; 
					fOut.write(GLOBAL_ABORT);
					sOut.write(GLOBAL_ABORT);
					break;
				}				
			}
			
			//Check votes from participants and either commit or abort
			if((votes[0] == VOTE_COMMIT) && (votes[1] == VOTE_COMMIT))
			{
				localLog = GLOBAL_COMMIT;
				fOut.write(GLOBAL_COMMIT);
				sOut.write(GLOBAL_COMMIT);				
			}
			else
			{
				localLog = GLOBAL_ABORT;
				fOut.write(GLOBAL_ABORT);
				sOut.write(GLOBAL_ABORT);	
			}
		}
	}
	
	
	
}
