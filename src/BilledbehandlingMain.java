import RobotControl.*;
	public class BilledbehandlingMain {
		public static void main(String[] args) throws InterruptedException {
			Billedbehandling bh = new Billedbehandling();
				
			bh.runImageRec();
			while (true)
			{
				//Thread.sleep(1000);
				//bh.doFrameReprint();
				bh.runImageRec();
			}
				
		}
	}