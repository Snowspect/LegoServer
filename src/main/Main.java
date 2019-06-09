package main;

import RobotControl.RemoteCarClient;

import java.io.IOException;

import Logic.RouteLogic;

public class Main {

	public static void main(String[] args) {

		// String ip = "192.168.0.17";
		String ip = "192.168.43.107";
		// String ip = "192.168.43.199";
		// String ip = "10.0.1.1";
		if (args.length > 0) {
			ip = args[0];
		}
		try {
			//activates the connection
			RemoteCarClient CarClient = new RemoteCarClient("R/C Client", ip);
			new Thread(CarClient).start();
			
			new RouteLogic(CarClient).running();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
