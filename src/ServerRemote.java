import java.io.*;
import java.net.*;
import lejos.hardware.*;
import lejos.hardware.motor.*;
import lejos.hardware.port.*;
import lejos.robotics.RegulatedMotor;

public class ServerRemote {

	public static final int port = 12345;
	private Socket client;
	private static boolean looping = true;
	private static ServerSocket server;
	private static UnregulatedMotor left = new UnregulatedMotor(MotorPort.A);
	//private static RegulatedMotor A = new EV3LargeRegulatedMotor(MotorPort.A);
	//private static EV3MediumRegulatedMotor A = new 
	private static RegulatedMotor B = new EV3LargeRegulatedMotor(MotorPort.B);
	private static RegulatedMotor C = new EV3LargeRegulatedMotor(MotorPort.C);
	
	public ServerRemote(Socket client) {
		this.client = client;
		
		Button.ESCAPE.addKeyListener(new EscapeListener());
	}
	
	public static void main(String[] args) throws IOException
	{
		server = new ServerSocket(port);
		while(looping)
		{
			System.out.println("Awaiting Client..");
			new ServerRemote(server.accept()).run();	
		}
	}
	public void carAction(int command) {
		switch(command) {
		case RemoteCarClient.BACKWARD:
			B.rotate(-360, true);
			C.rotate(-360);
			break;
		case RemoteCarClient.FORWARD:
			B.rotate(360, true);
			C.rotate(360);
			break;
		/*case RemoteCarClient.STRAIGHT:A.rotateTo(0);
			break;
		case RemoteCarClient.RIGHT:
			A.rotateTo(-170);
			break;
		case RemoteCarClient.LEFT:
			A.rotateTo(170);
			break;
		*/
	}
}

	public void run()
	{
		System.out.println("CLIENT CONNECT");
		try {
			InputStream in = client.getInputStream();
			DataInputStream dIn = new DataInputStream(in);
			
			while(client != null)
				{
					int command = dIn.readInt();
					System.out.println("REC: " + command);
					if(command == RemoteCarClient.CLOSE)
					{
						client.close();
						client = null;
						looping = false;
						System.exit(0);
					}
					else 
					{
						carAction(command);
					}
				}
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
private class EscapeListener implements KeyListener
	{

	@Override
	public void keyPressed(Key k) {
		looping = false;
		System.exit(0);
	}

	@Override
	public void keyReleased(Key k) {}
	}

	
}


