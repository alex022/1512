import java.io.IOException;
import java.net.UnknownHostException;

public class main {	
	
	public static void main(String args[]) throws UnknownHostException, IOException
	{	
		coordinator coord;
		participant p1, p2;
		
		int nodeNum = Integer.parseInt(args[0]); 		
		
		if(nodeNum == 0)
		{
			coord = new coordinator();
		}
		
		else if(nodeNum == 1)
		{
			p1 = new participant(nodeNum);	
		}
		else
		{
			p2 = new participant(nodeNum);
		}
				
	}	
}
