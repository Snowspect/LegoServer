import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class ServerRemote {

	public static final int port = 12345;
	private Socket client;
	private static boolean looping = true;
	private static boolean interupt = false; 
	private static ServerSocket server;
//	private static UnregulatedMotor left = new UnregulatedMotor(MotorPort.A);
	//private static RegulatedMotor A = new EV3LargeRegulatedMotor(MotorPort.A);
	//private static EV3MediumRegulatedMotor A = new 
	private static RegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
	private static RegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
	private static RegulatedMotor GrappleArm = new EV3MediumRegulatedMotor(MotorPort.C);
	private static RegulatedMotor ArmWheelMoter = new EV3MediumRegulatedMotor(MotorPort.D);
	private static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
	public ServerRemote(Socket client) {
		this.client = client;
		
		Button.ESCAPE.addKeyListener(new EscapeListener());
	}
	
	public static void main(String[] args) throws IOException
	{
		server = new ServerSocket(port);
		gyroSensor.reset();
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
			driveBackwards(400, 360, interupt);
			break;
		case RemoteCarClient.FORWARD: // W for activate
			driveForward(-400, 360, interupt);
			break;
		case RemoteCarClient.STOP: // Q for activate
			stopWheels();
			break;
		case RemoteCarClient.ARMUP:
			//For activate : F1
			GrappleArm.rotate(500,true);
			break;
		case RemoteCarClient.ARMDOWN:
			//For activate : F2
			GrappleArm.rotate(-500,true);
			break;
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
		case RemoteCarClient.GRAPPLEARMFUNCTION:
			//For activate : v
			Opsamling();
			break;
		case RemoteCarClient.UNLOAD:
			//For activate : P
			unload();
			break;
		case RemoteCarClient.TURNLEFT: //f4
			turnRight(200, 180, true);
			//gyroSensor.reset();
			break;
		case RemoteCarClient.TURNRIGHT: // f5
			turnLeft(200, 180, true);
			//gyroSensor.reset();
			break;
		case RemoteCarClient.PRINTGYRO:
			printGyro();
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
			
			System.out.println("Client Connected");
			while(client != null)
				{
					String commandString = dIn.readUTF();
					int command = Integer.parseInt(commandString);
					//int command = dIn.readInt();
					System.out.println("REC: " + command);
					if(command == RemoteCarClient.CLOSE) //escape for luk
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

//navigation methods
public void driveForward(int speed, int wheelrotation, boolean override) { // w for activate
	motorLeft.setSpeed(speed);
	motorRight.setSpeed(speed);
	motorRight.forward();
	motorLeft.forward();
	int counter = 0;
//	while(counter != wheelrotation) {
//		counter = motorRight.getTachoCount();
//	}
}
	
public void driveBackwards(int speed, int wheelrotation, boolean override) { // w for activate
	motorLeft.setSpeed(speed);
	motorRight.setSpeed(speed);
	motorRight.backward();
	motorLeft.backward();
	int counter = 0;
//		
}

public void fullStop() { // q for activate
	motorRight.setSpeed(0);
	motorLeft.setSpeed(0);
	
}

private void driveReverse() { // x for activate
	motorLeft.setSpeed(500);
	motorRight.setSpeed(500);
	motorLeft.backward();
	motorRight.backward();
}

	
public void turnLeft(int speed, int angle, boolean override){
	motorRight.setSpeed(speed);
	motorLeft.setSpeed(speed);
	motorRight.rotate(-angle, true);
	motorLeft.rotate(angle, true);
	
}


void turnRight(int speed, int angle, boolean override){
	motorRight.setSpeed(speed);
	motorLeft.setSpeed(speed);
	motorRight.rotate(angle, true);
	motorLeft.rotate(-angle, true);
	}



public void stopWheels() {
	motorRight.stop(true);
	motorLeft.stop(true);	
}




//Arm functions
private void Opsamling() {
	ArmWheelMoter.forward();
	grappleArmDown();
	try {
		Thread.sleep(3000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	grappleArmUp();
	try {
		Thread.sleep(3000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	ArmWheelMoter.stop();
	
	
}

private void grappleArmUp(){
	GrappleArm.rotate(500);
}

private void grappleArmDown() {
	GrappleArm.rotate(-500);
}

private void unload(){
	ArmWheelMoter.backward();
	try {
		Thread.sleep(15000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	ArmWheelMoter.stop();
}




public void printGyro() {
	
	final SampleProvider sp = gyroSensor.getAngleMode();
	int value = 0;
	float [] sample = new float[sp.sampleSize()];
    sp.fetchSample(sample, 0);
    value = (int)sample[0];
	System.out.println("Iteration: " + value);
	System.out.println("Gyro angle: " + value);
	gyroSensor.reset();
}


public void rotateForward() {

}

private void turnRightGyroImplementation(int angle) {

	final SampleProvider sp = gyroSensor.getAngleMode();
	int value = 0;
	
	motorLeft.setSpeed(300);
	motorRight.setSpeed(300);
	motorLeft.forward();
	motorRight.backward();



	while(true) {
    	float [] sample = new float[sp.sampleSize()];
        sp.fetchSample(sample, 0);
        value = (int)sample[0];

		if(value >= angle) {
			motorLeft.setSpeed(0);
			motorRight.setSpeed(0);
			motorLeft.stop();
			motorRight.stop();
			System.out.println("Iteration: " + value);
			System.out.println("Gyro angle: " + value);

	        
			if(value != 360) {
				turnleftGyroImplementation(360-value);
			}
			
			System.out.println("Resetting");
			gyroSensor.reset();
			break;
		}
	}
	

}

public void turnleftGyroImplementation(int angle) {
	motorLeft.setSpeed(300);
	motorRight.setSpeed(300);
	motorLeft.backward();
	motorRight.forward();

	final SampleProvider sp = gyroSensor.getAngleMode();
	int value = 0;

	while(true) {
    	float [] sample = new float[sp.sampleSize()];
        sp.fetchSample(sample, 0);
        value = (int)sample[0];
		
		if(value <= -angle) {
			motorLeft.setSpeed(0);
			motorRight.setSpeed(0);
			motorLeft.stop();
			motorRight.stop();
			System.out.println("Iteration: " + value);
			System.out.println("Gyro angle: " + value);
			
			if(value != -angle) {
				turnRightGyroImplementation(-360+angle);
				}
			}
			
			System.out.println("Resetting");
			gyroSensor.reset();
			break;
		}
	}

public void lockCarWhilePickup() {
	
}


}


