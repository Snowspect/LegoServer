import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

import RouteCalculator.PointInGrid;
import RouteCalculator.RouteCalculator;

public class RemoteCarClient extends Frame implements KeyListener {

	/// VARIABLES START ///
	public static final int PORT = ServerRemote.port;
	public static final int CLOSE = 27; //escape = luk program
	public static final int
	FORWARD = 40, //Pil op
	BACKWARD = 38, //pil ned
	TURNLEFT = 37, //pil left
	TURNRIGHT = 39, //pil right
	STOP = 81, //Q = Stops motors
	ARMUP = 66, //B = Arm goes UP
	ARMDOWN = 78, //N = arm goes down
	WHEELUP = 49, //num 1
	WHEELDOWN = 50, //num 2
	WHEELSTOP = 51, //num 3
	UNLOAD = 80, //p
	GRAPPLEARMFUNCTION = 86;// V = grappleFunction
	public static int [] [] GRID = new int [20][20];
	PRINTGYRO = 117;

	Button btnConnect;
	TextField txtIpAddress;
	TextArea messages;

	private Socket socket;
	private DataOutputStream outStream;
	static RouteCalculator rc;
	StringBuilder str;
	/// VARIABLES END ///

	/**
	 * @param title : unsure, suspect it sets the title of the GUI
	 * @param ip : the ip address in which we want to show in the GUI.
	 */
	public RemoteCarClient(String title, String ip)
	{
		//super(title);
		/*this.setSize(400, 300);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Ending  Warbird Client");
				disconnect();
				System.exit(0);
			}
		});*/
		//buildGUI(ip);
		//this.setVisible(true);
		//btnConnect.addKeyListener(this);
		rc = new RouteCalculator();
		str = new StringBuilder();
	}

	/**
	 * The ip is set here.
	 * @param args
	 */
	public static void main(String args[])
	{
		//String ip = "192.168.0.17";
		String ip = "192.168.43.107";
		//String ip = "10.0.1.1";
		if(args.length > 0) {
			ip = args[0];
		}
		System.out.println("Starting Client...");
		new RemoteCarClient("R/C Client", ip);

		//Method contains the same code as roadTrip(). Use for testing
		rc.getDir(new PointInGrid(1076,1916), new PointInGrid(1074,1915), new PointInGrid(625,713));

	}

	/**
	 * Calculates a path from one point to another in a grid
	 * @param conPoint : one of four points around the corners of the grid???
	 * @param posPoint : where we are located.
	 * @param destPoint : Where we are going ultimately.
	 */
	public void roadtrip(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint) {
		int destRow = destPoint.getX(), destCol = destPoint.getY();
		int posRow = posPoint.getX(), posCol = posPoint.getY();
		int conRow = conPoint.getX(), conCol = conPoint.getY();
		// These strings tells us which function we call on the robot
		String OF = "0F:0;";
		String OG = "0G:0;";
		String OS = "0S:0;";
		String LR = "LR:0;";
		String RR = "RR:0;";
		String OB = "0B:false";

		//Calculates the angle from one point to another.
		double angle = rc.calc_Angle(conRow, conCol, destRow, destCol, posRow, posCol);
		System.out.println("--------- NOT ABS ----------");
		System.out.println("ANGLE: "+ angle);
		System.out.println("ANGLE2: "+ (360 - angle)+"\n");

		System.out.println("--------- WITH ABS ---------");
		System.out.printf("ANGLE: %.2f\n", Math.abs(angle));
		System.out.printf("ANGLE2: %.2f\n", (360 - Math.abs(angle)));

		// If angle > 0: Turn right, else if angle < 0: Turn left

		//calculates the distance from one point to another
		double dist = rc.calc_Dist(posPoint, destPoint);
		System.out.printf("Distance: %.2f", dist);

		if (angle > 0) {
			OF = "0F:4;";
			RR = "RR:"+Math.round(angle)+";";
		} else if (angle < 0) {
			OF = "0F:3;";
			LR = "LR:"+Math.round(Math.abs(angle))+";";
		} else if (angle == 0) {
			OF = "0F:1;";
			if (dist > 300)
				OS = "0S:150;";
			else OS = "0S:50;";
		}

		//creates the string to send for the robot to interpret
		str.append(OF);
		str.append(OG);
		str.append(OS);
		str.append(LR);
		str.append(RR);
		str.append(OB);

		String COMMAND = str.toString();

		System.out.println(COMMAND);

	}

	/**
	 * Sends the string for the robot to interpret.
	 * @param command : currently the number which is written to the server
	 */
	public void SendCommandString(String command)
	{
		//Send coordinates to the server
		messages.setText("Status: sending command");
		try {
			outStream.writeUTF(command);
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
					socket = new Socket(txtIpAddress.getText(), PORT);
					outStream = new DataOutputStream(socket.getOutputStream());
					messages.setText("Status: CONNECTED");

					btnConnect.setLabel("disconnect");
				} catch (Exception exc) {
					messages.setText("status: FAILURE Error establishing connection with server.");
					System.out.println("Error: " + exc);
				}
			} else if (command.equals("Disconnect"))
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
}
