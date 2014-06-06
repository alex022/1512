import java.io.IOException;
import java.io.InputStream;
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
	
	Thread participant1;	
	Thread participant2;
	
	private PrintWriter fOut;
	private PrintWriter sOut; 
	private InputStream fIn;
	private InputStream sIn;
	
	private static int vote1, vote2;
	private static int localLog;
	
	private long time;	
	
	public coordinator()
	{
		localLog= START_2PC;
		vote1 = vote2 = 0;
		
		participant1 = new Thread(new coordinatorThread(1));		
		participant2 = new Thread(new coordinatorThread(2));
		participant1.start();
		participant2.start();
	}
	
	class coordinatorThread implements Runnable
	{
		ServerSocket serverSocket;
		Socket socket;
		PrintWriter out;
		InputStream in; 
		int node;
		
		
		public coordinatorThread(int node)
		{
			this.node = node;
			
			try {
				serverSocket = new ServerSocket(5000 + node);
				System.out.println("Waiting for participant " + node + " to connect...");
				socket = serverSocket.accept();
				System.out.println("Participant " + node + " has connected");
				
				out = new PrintWriter(socket.getOutputStream());
				in = socket.getInputStream();
				System.out.println("Initialized input/output streams for participant " + node); 
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		@Override
		public void run() {
			while(localLog == START_2PC)
			{
									
					out.write(VOTE_REQUEST);
				
				 
				System.out.println("Coordinator sent VOTE_REQUEST to participant " + node);
			}
			
		}
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
			while(vote1 == 0 && vote2 == 0)
			{		
				System.out.println("Coordinator is reading votes from participants");
				vote1 = fIn.read(); 
				vote2 = sIn.read();
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
			if((vote1 == VOTE_COMMIT) && (vote2 == VOTE_COMMIT))
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



	
	
	
}
