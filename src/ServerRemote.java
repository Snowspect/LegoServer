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
//	private static UnregulatedMotor left = new UnregulatedMotor(MotorPort.A);
	//private static RegulatedMotor A = new EV3LargeRegulatedMotor(MotorPort.A);
	//private static EV3MediumRegulatedMotor A = new 
	private static RegulatedMotor A = new EV3LargeRegulatedMotor(MotorPort.A);
	private static RegulatedMotor B = new EV3LargeRegulatedMotor(MotorPort.B);
	private static RegulatedMotor GrappleArm = new EV3MediumRegulatedMotor(MotorPort.C);
	private static RegulatedMotor ArmWheelMoter = new EV3MediumRegulatedMotor(MotorPort.D);
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
			//For activate : X
			A.setSpeed(1000);
			B.setSpeed(1000);
			A.backward();
			B.backward();
			//A.rotate(-360, true);
			//B.rotate(-360);
			break;
		case RemoteCarClient.FORWARD:
			//For activate : W
			A.setSpeed(1000);
			B.setSpeed(1000);
			B.forward();
			A.forward();
			GrappleArm.rotate(80,true);
			//A.rotate(360, true);
			//B.rotate(360);
			break;
		case RemoteCarClient.STOP:
			//For activate : Q
			B.setSpeed(0);
			A.setSpeed(0);
		//case RemoteCarClient.STRAIGHT://A.rotateTo(0);
		case RemoteCarClient.ARMUP:
			//For activate : T
			GrappleArm.rotate(-440,true);
			break;
		case RemoteCarClient.ARMDOWN:
			//For activate : U
			GrappleArm.rotate(-440,true);
			break;
//			break;
		case RemoteCarClient.WHEELUP:
			//For activate : 1
			ArmWheelMoter.backward();
			break;
		case RemoteCarClient.WHEELDOWN:
			//For activate : 2
			ArmWheelMoter.forward();
			break;
		case RemoteCarClient.WHEELSTOP:
			//For activate : 3
			ArmWheelMoter.stop();
			break;
		case RemoteCarClient.grappleArmFunction:
			//For activate : F1
			Opsamling();
			break;
		/*case RemoteCarClient.RIGHT:
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

//Herfra skrives diverse funktioner til robotten



private void Opsamling() {
	ArmWheelMoter.backward();
	grappleArmDown();
	try {
		Thread.sleep(3000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	grappleArmUp();
	ArmWheelMoter.stop();
	
	
}

private void grappleArmUp(){
	GrappleArm.rotate(440,true);
}

private void grappleArmDown() {
	GrappleArm.rotate(-440,true);
}


}


