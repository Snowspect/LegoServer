import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class RemoteCarClient extends Frame implements KeyListener {
	public static final int PORT = ServerRemote.port;
	public static final int CLOSE = Q;
	public static final int FORWARD = 87, //W = main up
	STRAIGHT = 83, // S = straight
	LEFT = 65, // A = LEFT
	RIGHT = 68, //D = RIGHT
	BACKWARD = 88; //X = main down
	
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
		String ip = "10.0.1.1";
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
	
}
