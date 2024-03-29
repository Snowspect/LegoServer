package main;

import RobotControl.Billedbehandling;
import RobotControl.RemoteCarClient;

import java.io.IOException;

import Logic.RouteLogic;


public class Main {

	public static RemoteCarClient RC;
	public static Billedbehandling ImageRec;
	
	public static void main(String[] args) throws InterruptedException {

//		new RouteLogic().runningTwo();
		
		// String ip = "192.168.0.17";
		String ip = "192.168.43.107";
		// String ip = "192.168.43.199";
		// String ip = "10.0.1.1";
//		if (args.length > 0) {
//			ip = args[0];
//		}
		try {
			//activates the connection and allows for communication with thread
			RC = new RemoteCarClient("R/C Client", ip);
			Thread thread = new Thread(RC);
			thread.setDaemon(true);
			thread.start();
//			
			Billedbehandling BB = new Billedbehandling();
			System.out.println("initiating sleep");
			//Thread.sleep(90000);
			System.out.println("past sleep");
//          while()
//			new RouteLogic(BB);
//			
//			ImageRec = new Billedbehandling_27032019();
//			Thread thread2 = new Thread(ImageRec);
//			thread2.setDaemon(true);
//			thread2.start();
			
			new RouteLogic(BB).runningTwo();
			System.exit(0);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}