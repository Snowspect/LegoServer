package RobotControl;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import RouteCalculator.PointInGrid;
import RouteCalculator.RouteCalculator;

public class RemoteCarClient extends Frame implements KeyListener, Runnable {

	/// VARIABLES START ///
	public static final int PORT = ServerRemote.port;
	public static final int CLOSE = 27; //escape = luk program
	public static final int FORWARD = 38, //pil op
			STOP = 81, //Q = Stops motors
	TURNLEFT = 37, // Pil venstre
	TURNRIGHT = 39, // Pil højre
	BACKWARD = 40, //pil ned
	ARMUP = 88, // X
	ARMDOWN = 90, // Z
	WHEELUP = 83, // S
	WHEELDOWN = 65, // A
	WHEELSTOP = 68, // D
	UNLOAD = 80, //P = unload
	GRAPPLEARMFUNCTION = 86; // V = grappleFunction ------------ FIX
	
	
	public static boolean waiting;
	public static int [] [] GRID = new int [20][20];

		PointInGrid [] checkPoints = { new PointInGrid(480, 270), new PointInGrid(1440, 270),
										new PointInGrid(480, 810), new PointInGrid(1440, 810) };
	Button btnConnect;
	TextField txtIpAddress;
	TextArea messages;

	private Socket socket;
	private DataOutputStream outStream;
	private DataInputStream inStream;
	static RouteCalculator rc;
	static RemoteCarClient RC;
		StringBuilder str;

	/**
	 * @param title : unsure, suspect it sets the title of the GUI
	 * @param ip : the ip address in which we want to show in the GUI.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public RemoteCarClient(String title, String ip) throws UnknownHostException, IOException
	{
		super(title);
		System.out.println("Starting Client...");
		this.setSize(400, 300);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Ending  Warbird Client");
				disconnect();
				System.exit(0);
			}
		});
		buildGUI(ip);
		this.setVisible(true);
		btnConnect.addKeyListener(this);
		socket = new Socket(txtIpAddress.getText(), PORT);
		outStream = new DataOutputStream(socket.getOutputStream());
		//rc = new RouteCalculator();
	}


	/**
	 * Calculates a path from one point to another in a grid
	 * @param conPoint : Fixed point a few pixels directly in front of robot
	 * @param posPoint : where we are located.
	 * @param destPoint : Where we are going ultimately.
	 */
	public void roadtrip(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint, boolean goingOverNOGO) {

		String COMMAND;

//		if (Math.abs(posPoint.getX()-destPoint.getX()) > RouteCalculator.TrackWidth/2
//				&& Math.abs(posPoint.getY()-destPoint.getY()) > RouteCalculator.TrackLenght/2)

		
			COMMAND = rc.getDir(conPoint, posPoint, destPoint);


		System.out.println(COMMAND);

	}

	 /* Sends the string for the robot to interpret.
	 * @param command : currently the number which is written to the server
	 */
	public void SendCommandString(String command)
	{
		waiting = true;
		//Send coordinates to the server
		messages.setText("Status: sending command");
		try {
			System.out.println("This was passed to sendCommand : " + command);
			//System.out.println(outStream);
			outStream.writeUTF(command);
			//waiting = true;
			System.out.println("Command sent");
		} catch (IOException io)
		{
			messages.setText("Status: Error problems occurred sending data");
		}
		messages.setText("status: command sent");
	}


	/**
	 * shuts down both server and client
	 */
	public void disconnect()
	{
		try {
			SendCommandString(CLOSE + "");
			socket.close();

			btnConnect.setLabel("Connect");
			messages.setText("Status: DISCONNECTED");
		} catch (Exception exc)
		{
			messages.setText("status: Failure Error closing connection with server");
			System.out.println("error: " + exc);
		}
	}

	/**
	 * Registers the pressed key
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		SendCommandString(e.getKeyCode() + "");
		System.out.println("Pressed " + e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}

	/**
	 * A listener class for all the buttons of the GUI
	 * (The gui in which you connect to the server car)
	 * no need to change this at any point in the future.
	 */
	private class ControlListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("Connect")) {
				try {
					//outStream = new DataOutputStream(socket.getOutputStream());
					messages.setText("Status: CONNECTED");

					btnConnect.setLabel("disconnect");
				} catch (Exception exc) {
					messages.setText("status: FAILURE Error establishing connection with server.");
					System.out.println("Error: " + exc);
				}
			} else if (command.equals("disconnect"))
			{
				disconnect();
			}
		}
	}

	/**
	 * Builds the client GUI to be able to connect to the robot.
	 * @param ip : The ip which it connects to, defined in main.
	 */
	public void buildGUI(String ip)
	{
		Panel mainPanel = new Panel (new BorderLayout());
		ControlListener cl = new ControlListener();

		btnConnect = new Button("Connect");
		btnConnect.addActionListener(cl);

		txtIpAddress = new TextField(ip,16);

		messages = new TextArea("status: DISCONNECTED");
		messages.setEditable(false);

		Panel north = new Panel(new FlowLayout(FlowLayout.LEFT));
		north.add(btnConnect);
		north.add(txtIpAddress);

		Panel center = new Panel(new GridLayout(5,1));
		center.add(new Label("A-S-D to steer, W-X to move"));

		Panel center4 = new Panel(new FlowLayout(FlowLayout.LEFT));
		center4.add(messages);

		center.add(center4);

		mainPanel.add(north, "North");
		mainPanel.add(center, "Center");
		this.add(mainPanel);
	}


	public void SendCommand(String command)
	{
		//Send coordinates to the server
		//messages.setText("Status: sending command");
		System.out.println("Sending command: " + command);
		/*try {
			outStream.writeUTF(command);
		} catch (IOException io) {
			System.out.println("Status: Error problems occurred sending data");
		}
		*/
		//messages.setText("status: command sent");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		boolean stillRunning = true;
		while (stillRunning) {
			String reading = "..";
			try {
				inStream = new DataInputStream(socket.getInputStream());
				reading = inStream.readUTF();
				if(reading.equalsIgnoreCase("recieved"))
				{
					System.out.println("read recieved");
					waiting = false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("hit the IO Exception");
				e.printStackTrace();
			}
			System.out.println(reading);
		}
	}
	public boolean GetSendingStatus()
	{
		return waiting;
	}
}