import RobotControl.*;
	public class BilledbehandlingMain {
		public static void main(String[] args) throws InterruptedException {
			Billedbehandling bh = new Billedbehandling();
			
			while (true)
			{
				//Thread.sleep(1000);
				//bh.doFrameReprint();
				bh.runImageRec();
				bh.getCorners();
			}
				
		}
	}