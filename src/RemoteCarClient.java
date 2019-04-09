import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class RemoteCarClient extends Frame implements KeyListener {
	public static final int PORT = ServerRemote.port;
	public static final int CLOSE = 27; //escape = luk program
	public static final int FORWARD = 40, //W = main up
	STOP = 81, //Q = Stops motors
	BACKWARD = 38, //X = main down
	ARMUP = 66, //B = Arm goes UP
	ARMDOWN = 78, //N = arm goes down
	WHEELUP = 49, //1
	WHEELDOWN = 50, //2
	WHEELSTOP = 51, //3
	UNLOAD = 80, //P for unload
	GRAPPLEARMFUNCTION = 86,// V = grappleFunction
	TURNLEFT = 37, //F4
	TURNRIGHT = 39, //F5
	PRINTGYRO = 117; 
	
	
	
	Button btnConnect;
	TextField txtIpAddress;
	TextArea messages;
	
	private Socket socket;
	private DataOutputStream outStream;
	
	public RemoteCarClient(String title, String ip)
	{
		super(title);
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
	}
	
	public static void main(String args[])
	{
		String ip = "192.168.43.107";
		//String ip = "10.0.1.1";
		if(args.length > 0) {
			ip = args[0];
		}
		System.out.println("Starting Client...");
		new RemoteCarClient("R/C Client", ip);
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
		messages.setText("Status: sending command");
		try {

			outStream.writeInt(command);
		} catch (IOException io)
		{
			messages.setText("Status: Error problems occurred sending data");
		}
		
		messages.setText("status: command sent");
	}
	
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
		//SendCommand(e.getKeyCode());
		SendCommandString(e.getKeyCode() + "");
		System.out.println("Pressed " + e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
