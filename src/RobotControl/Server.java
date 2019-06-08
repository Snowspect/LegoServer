package RobotControl;
import java.io.*;
import java.net.*;


import lejos.hardware.Battery;

public class Server {
	
	public static final int port = 1234;
	
	public static void main(String[] args) throws IOException
	{
		ServerSocket server = new ServerSocket(port);
		System.out.println("awaiting client");
		Socket client = server.accept();
		System.out.println("Connected");
		OutputStream out = client.getOutputStream();
		
		DataOutputStream dOut2 = new DataOutputStream(out);
		DataOutputStream dOut = new DataOutputStream(out);
		dOut.writeUTF("Battery: " + Battery.getVoltage());
		dOut.flush();
		server.close();
	}
}
