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

	/// VARIABLES START ///
	
				///old motors///
	//private static UnregulatedMotor left = new UnregulatedMotor(MotorPort.A);
	//private static RegulatedMotor A = new EV3LargeRegulatedMotor(MotorPort.A);
	//private static EV3MediumRegulatedMotor A = new 
				///old motors end ///
	
	int functionInt;
	double grades, speed, leftRotation, rightRotation;
	public static final int port = 12345;
	private Socket client;
	private static boolean looping = true;
	private static boolean interrupt = false; 
	private static ServerSocket server;
	private static RegulatedMotor motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
	private static RegulatedMotor motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
	private static RegulatedMotor GrappleArm = new EV3MediumRegulatedMotor(MotorPort.C);
	private static RegulatedMotor ArmWheelMoter = new EV3MediumRegulatedMotor(MotorPort.D);
	private static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);
	/// VARIABLES END ///
	
	/**
	 * sets global client to remote requesting client
	 * @param client : the client which we recieved a connection request from.
	 */
	public ServerRemote(Socket client) {
		this.client = client;
		
		Button.ESCAPE.addKeyListener(new EscapeListener());
	}
	
	/**
	 * starts the server and awaits client ((accepts client when it connects))
	 */
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
	
	/**
	 * activates a specific method based on the passed integer
	 * @param command : The integer that decides what function to trigger
	 */
	public void carAction(int command) {
		switch(command) {
		case RemoteCarClient.BACKWARD:
			//For activate : X
			driveBackwards(400, 360, interrupt);
			break;
		case RemoteCarClient.FORWARD: // W for activate
			driveForward(-400, 360, interrupt);
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

	/**
	 * Reads the UTF (String) send by the client, parse it and sends it to carAction
	 * can also close server
	 */
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
					
					String splitter = "0F:2;0G:200;0S:300;LR:40;RR:50;0B:true";
					Interrupter(splitter); //sets values og global variables. 
					
					//currently not using the Interrupter method, but it is simply implemented
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

/**
 * button to exit system
 */
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
/**
 * Drives forward
 * @param speed : the speed in which we go forward
 * @param wheelrotation : How far we want to go forward in degrees
 * @param override : If we want to stop going forward
 */
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
	
/**
 * Drives backwards
 * @param speed : the speed in which we drive backwards
 * @param wheelrotation : how far we want to go backwards in Degrees
 * @param override : if we want to stop going backwards
 */
public void driveBackwards(int speed, int wheelrotation, boolean override) { // w for activate
	motorLeft.setSpeed(speed);
	motorRight.setSpeed(speed);
	motorRight.backward();
	motorLeft.backward();
	int counter = 0;
//		
}

/**
 * Stops the car
 */
public void fullStop() { // q for activate
	motorRight.setSpeed(0);
	motorLeft.setSpeed(0);
	
}

/**
 * drive backwards
 */
private void driveReverse() { // x for activate
	motorLeft.setSpeed(500);
	motorRight.setSpeed(500);
	motorLeft.backward();
	motorRight.backward();
}

/**
 * turns the robot to the left
 * @param speed : the speed in which we turn
 * @param angle : the angle we rotate for the wheels
 * @param override : if we want to stop the turning
 */	
public void turnLeft(int speed, int angle, boolean override){
	motorRight.setSpeed(speed);
	motorLeft.setSpeed(speed);
	motorRight.rotate(-angle, true);
	motorLeft.rotate(angle, true);
	
}


/**
 * turns the robot to the right
 * @param speed : the speed in which we turn
 * @param angle : the angle we rotate for the wheels
 * @param override : if we want to stop the turning
 */
void turnRight(int speed, int angle, boolean override){
	motorRight.setSpeed(speed);
	motorLeft.setSpeed(speed);
	motorRight.rotate(angle, true);
	motorLeft.rotate(-angle, true);
	}

/**
 * Stops the robot
 */
public void stopWheels() {
	motorRight.stop(true);
	motorLeft.stop(true);	
}


//Arm functions
/**
 * Activates pickup wheel, moves arm down, moves arm up.
 */
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

/**
 * moves the arm up
 */
private void grappleArmUp(){
	GrappleArm.rotate(500);
}

/**
 * moves the arm down
 */
private void grappleArmDown() {
	GrappleArm.rotate(-500);
}

/**
 * moves wheel on arm inwards (so the balls get pushed out)
 */
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

/**
 * Prints the gyro sensor value to console
 */
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

/**
 * NEEDED?
 */
public void rotateForward() {

}

/**
 * rotates the car around itself by an angle (to the right)
 * @param angle : the amount of degrees we turn around ourselves by gyro standard.
 */
private void turnRightGyroImplementation(double angle) {

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

/**
 * rotates the car around itself by an angle (to the left)
 * @param angle : the amount of degrees we turn around ourselves by gyro standard.
 */
public void turnleftGyroImplementation(double angle) {
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

/**
 * Not implemented yet
 */
public void lockCarWhilePickup() {
	
}

/**
 * Splits the client string into substrings for the functions to use
 * @param clientString : The string that gets splitted
 */
public void Interrupter(String clientString) //takes format : 
{
	String[] Values = clientString.split(";");
	for (String value : Values) {
		if(value.contains("0F"))
		{
			functionInt = Integer.parseInt(value.substring(3));
		}
		if(value.contains("0G"))
		{
			grades = Double.parseDouble(value.substring(3));
		}
		if(value.contains("0S"))
		{
			speed = Double.parseDouble(value.substring(3));
		}
		if(value.contains("LR"))
		{
			leftRotation = Double.parseDouble(value.substring(3));
		}
		if(value.contains("RR"))
		{
			rightRotation = Double.parseDouble(value.substring(3));
		}
		if(value.contains("0B"))
		{
			interrupt = Boolean.parseBoolean(value.substring(3));
		}
	}	
}

}


