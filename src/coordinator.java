import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class coordinator {
	
	private final int VOTE_REQUEST = 1; 
	private final int GLOBAL_ABORT = 2;
	private final int GLOBAL_COMMIT = 3;
	private final int COMMIT = 4;
	private final int VOTE_COMMIT = 5; 
	private final int START_2PC = 6;
	private final int INIT = 7;
	
	private final int TIMEOUT = 20000;
	
	private ServerSocket fParticipant;	
	private ServerSocket sParticipant;
	private Socket p1;
	private Socket p2; 
	
	private PrintWriter fOut;
	private PrintWriter sOut; 
	private BufferedReader fIn;
	private BufferedReader sIn;
	
	private int votes[];
	private int localLog;
	
	private long time;
	
	
	public coordinator()
	{
		localLog= START_2PC;
		votes = new int[2];
		votes[0] = 0; votes[1] = 0;
		
		Thread thread = new Thread(new coordinatorThread());
		thread.start();
	}
		
	public void coordinate() throws IOException
	{
		while(localLog == START_2PC)
		{
			//Multicast vote request to all participants			
			fOut.write(VOTE_REQUEST);
			sOut.write(VOTE_REQUEST);
			System.out.println("Coordinator multicasted VOTE_REQUEST");
			
			time = System.currentTimeMillis(); 
			
			//Wait for all votes to be collected
			while(votes[0] == 0 && votes[1] == 0)
			{		
				System.out.println("Coordinator is reading votes from participants");
				votes[0] = fIn.read(); 
				votes[1] = sIn.read();
				System.out.println("Finished reading votes from participants"); 
				
				//If loop times out, record and broadcast global abort and exit
				if((System.currentTimeMillis() - time) > TIMEOUT)
				{
					localLog = GLOBAL_ABORT; 
					fOut.write(GLOBAL_ABORT);
					sOut.write(GLOBAL_ABORT);
					System.out.println("Coordinator has timed out and multicasted GLOBAL_ABORT"); 
					break;
				}				
			}
			
			//Check votes from participants and either commit or abort
			if((votes[0] == VOTE_COMMIT) && (votes[1] == VOTE_COMMIT))
			{
				System.out.println("Coordinator received VOTE_COMMIT from two participants, multicasting GLOBAL_COMMIT"); 
				localLog = GLOBAL_COMMIT;
				fOut.write(GLOBAL_COMMIT);
				sOut.write(GLOBAL_COMMIT);				
			}
			else
			{
				System.out.println("One or more participants has voted VOTE_ABORT, multicasting GLOBAL_ABORT"); 
				localLog = GLOBAL_ABORT;
				fOut.write(GLOBAL_ABORT);
				sOut.write(GLOBAL_ABORT);	
			}
		}
	}
	
	class coordinatorThread implements Runnable
	{
		public coordinatorThread()
		{
			//Initialize sockets, writers, and readersr
			try{
				fParticipant = new ServerSocket(5000);
				sParticipant = new ServerSocket(5001);
				
				System.out.println("Waiting for participants to connect..."); 
				
				p1 = fParticipant.accept();
				System.out.println("Initialized first participant socket");
				p2 = sParticipant.accept();
				System.out.println("Initialized second participant socket"); 					
				
				fOut = new PrintWriter(p1.getOutputStream(), true);
				sOut = new PrintWriter(p2.getOutputStream(), true);
				
				fIn = new BufferedReader(new InputStreamReader(p1.getInputStream()));
				sIn = new BufferedReader(new InputStreamReader(p2.getInputStream()));
			} catch(Exception e)
			{
				System.out.println("Failed to open socket"); 
			}
		}

		@Override
		public void run() {
			try {
				coordinate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
	}
}
