import RobotControl.*;

public class BilledbehandlingMain {
	public static void main(String[] args) throws InterruptedException {
		Billedbehandling bh = new Billedbehandling();
		
		while (true)
		{
			
			long startTime = System.nanoTime();
						
			bh.runImageRec();			
			
			long totalTime = System.nanoTime() - startTime;			
			double seconds = (double)totalTime / 1000000000.0;
			System.out.println("OVERALL TIME : " + seconds + " seconds");
			
		} // End of while loop
	} // End of main
} // End of constructor