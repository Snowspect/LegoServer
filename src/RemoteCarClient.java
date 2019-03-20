import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class RemoteCarClient extends Frame implements KeyListener {
	public static final int PORT = ServerRemote.port;
	public static final int CLOSE = 27; //escape = luk program
	public static final int FORWARD = 87, //W = main up
			STOP = 81, //Q = Stops motors
	LEFT = 65, // A = LEFT
	RIGHT = 68, //D = RIGHT
	BACKWARD = 88, //X = main down
	ARMUP = 112, //pil op = Arm goes UP
	ARMDOWN = 113, // pil ned = arm goes down
	WHEELUP = 49,
	WHEELDOWN = 50,
	WHEELSTOP = 51,
	UNLOAD = 80, //P for unload
	GRAPPLEARMFUNCTION = 86,// V = grappleFunction	
	TURNLEFT = 115, //F4
	TURNRIGHT = 116, //F5
	PRINTGYRO = 117; 
	public static int [] [] GRID = new int [20][20];
	
	Button btnConnect;
	TextField txtIpAddress;
	TextArea messages;
	
	private Socket socket;
	private DataOutputStream outStream;
	
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
	}
	
	public static void main(String args[])
	{
		
		String ip = "192.168.43.199";
		//String ip = "10.0.1.1";
		if(args.length > 0) {
			ip = args[0];
		}
		System.out.println("Starting Client...");
		//new RemoteCarClient("R/C Client", ip);
		RemoteCarClient car = new RemoteCarClient("ghjklæ", ip);
		car.getDir(56, 56);
		
	}
	
	public double calc_Angle(int x1, int y1, int x2, int y2) {
		
		double diffx = x2 - x1;
		double diffy = y2 - y1;
		double angle = Math.atan2((x1 - x2), (y1 - y2)) * 180 / Math.PI;
		if (angle < 0)
			return angle;
		else 
			return angle;
	}
	
	public void getDir(int pos, int dir) {
		int dirRow = dir/20, dirCol = dir - (dirRow*20);
		int posRow = pos/20, posCol = pos - (posRow*20);
		
		double angle = calc_Angle(posCol+1, posRow, dirCol, dirRow);
		System.out.println("uhbjnklmmnjbhgfcgvhbjkml  		"+angle);
		
		if (angle > 0) {
			SendCommand(RIGHT);
		} else if (angle < 0) {
			SendCommand(LEFT);
		} else {
			//Kør lige ud eller bagud
		}
		
	}
	
	public void getRowDir(int pos, int dir) {
		int dirRow = dir/20;
		int posRow = pos/20;
		
		if (posRow < dirRow) {
			SendCommand(LEFT);
		} else if (posRow> dirRow) {
			SendCommand(RIGHT);
		} else {
			SendCommand(WHEELUP);
		}
	}
	
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
	
	public void SendCommand(int command)
	{
		//Send coordinates to the server
		//messages.setText("Status: sending command");
		System.out.println("Sending command: " + command);
		/*try {
			outStream.writeInt(command);
		} catch (IOException io) {
			System.out.println("Status: Error problems occurred sending data");
		}
		*/
		//messages.setText("status: command sent");
	}
	
	/**A listener class for all the buttons of the GUI */
	
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
	public void disconnect()
	{
		try {
			SendCommand(CLOSE);
			socket.close();
			
			btnConnect.setLabel("Connect");
			messages.setText("Status: DISCONNECTED");
		} catch (Exception exc)
		{
			messages.setText("status: Failure Error closing connection with server");
			System.out.println("error: " + exc);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		SendCommand(e.getKeyCode());
		System.out.println("Pressed " + e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
		
	
}
