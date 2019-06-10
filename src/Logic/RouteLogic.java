package Logic;

import RouteCalculator.RouteCalculator;
import RouteCalculator.RouteCalculatorInterface;
import lejos.hardware.sensor.NXTUltrasonicSensor.DistanceMode;
import lejos.robotics.navigation.Ballbot;
import main.Main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import RobotControl.RemoteCarClient;
import RouteCalculator.PointInGrid;

public class RouteLogic implements IRouteLogic, Runnable {
	
	////SIMULATIONGRID AND SIMULATIONBALLS////
	private int[][] SimulatedGrid = new int[20][20];
	private List<PointInGrid> listofBallCoords = new ArrayList<PointInGrid>();
	
	//NOT SIMULATION VARIABLES//
	private int ballvalue = 4;
	private int checkpoint;
	private List<PointInGrid> coordsOnPath;
	private PointInGrid robotMiddle, robotFront;
	private PointInGrid firstConnection, LastTouchedConnectionPoint, newConnectionPoint, branchOffPoint;
	private List<PointInGrid> Balls, ConnectionPoints; 
	private int[][] ImageGrid;
	private final int OBSTACLE = 1;
	boolean firstConnectionFound,firstConnectionTouched, programStillRunning, branchOff;
	private RemoteCarClient RC;
	private RouteCalculatorInterface Calculator;
	Scanner keyb = new Scanner(System.in);
	
	public RouteLogic() {
		this.Calculator = new RouteCalculator();
		//this.RC = Main.RC;
		

	}
	
	/**
	 * Constructor for the class
	 * @param robotMiddle rotaionCenter on robot
	 * @param robotFront Point right in front of robot
	 * @param Balls Locations of the balls on the track
	 * @param ConnectionPoints Four connectionPoints posing for the robot's overall path
	 * @param ImageGrid 2D array that imitates the entire track
	 */
	public RouteLogic(PointInGrid robotMiddle, PointInGrid robotFront, List<PointInGrid> Balls, List<PointInGrid> ConnectionPoints, int[][] ImageGrid) 
	{
		this.robotMiddle = robotMiddle;
		this.robotFront = robotFront;
		this.Balls = Balls;
		this.ConnectionPoints = ConnectionPoints;
		this.ImageGrid = ImageGrid;
		this.firstConnectionFound = false;
		this.Calculator = new RouteCalculator();				
	}
	
	/**
	 * Finds the closest connection point that has a direct path in relation to the robot middle
	 * @param robotMiddle rotationCenter on robot
	 * @param ConnectionPoints Four connectionPoints posing for the robot's overall path
	 */
	private PointInGrid findFirstConnectionPoint(PointInGrid robotMiddle, List<PointInGrid> ConnectionPoints) {
		double dist = 10000;
		PointInGrid closestPoint = null;
		
		List<PointInGrid> connPointWithDirectPath = new ArrayList<PointInGrid>();
		//checks if the points have a direct path
		for (PointInGrid connPoint : ConnectionPoints) {
			if(checkDirectPath(pointsOnRoute(robotMiddle, connPoint)) == true) {
				connPointWithDirectPath.add(connPoint);
			}
		}

		//finds the closest of those with direct path
		for (PointInGrid connPoint : connPointWithDirectPath) {
			if (Calculator.calc_Dist(robotMiddle, connPoint) < dist) {
			dist = Calculator.calc_Dist(robotMiddle, connPoint);
			closestPoint = connPoint;
			}
		}
		return closestPoint;
	}
	
	/**
	 * This method computes where the robot shall go based on several params
	 */
	public void running() {
		//INITIALIZE FOR TEST DATA
		this.ConnectionPoints = new ArrayList<PointInGrid>();
		this.Balls = new ArrayList<PointInGrid>();
		//CommunicateToServer("0F:1;0G:0;0S:300;LR:200;RR:200;0B:true;");
		
		////SIMULATION START////
		CreateGrid(); //creates a artificial grid to use in simulation
		findElementsInGrid(); //finds balls, robot points and connectionpoints
		
		//TODO take picture and get initial elements
		
		this.programStillRunning = true;
		int counter =  0;
		while (this.programStillRunning) {
			//System.out.println("waiting status : " + RC.GetSendingStatus());
			//if (RC.GetSendingStatus() == false) { //if the sending status returned is false	
			PointInGrid nearestBall;
			//TODO take picture and get elements
			/*if(counter == 0)
			{
				CommunicateToServer(command);
			}*/
			
			
			//find all balls with a direct path
			List<PointInGrid> ballsWithDirectPathFromRobot = BallsWithDirectPath(robotMiddle, Balls);
			
			System.out.println(ballsWithDirectPathFromRobot.size());
			//if the list isn't empty
			if(!ballsWithDirectPathFromRobot.isEmpty())
			{
				System.out.println("Direct path found");
				nearestBall = findNearestBall(robotMiddle, ballsWithDirectPathFromRobot);
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, nearestBall);
				keyb.next();
				System.out.println("this is the command send to server :" + commandToSend);
				CommunicateToServer(commandToSend);
				if(counter == 0) {
					robotFront = new PointInGrid(4,5);
					findElementsInGrid();
					counter++;
				}
				else if(counter == 1) { 
					robotFront = new PointInGrid(3,6); robotMiddle = new PointInGrid(4,5); 
					Balls.remove(Balls.lastIndexOf(nearestBall));
					findElementsInGrid();
					String pickup = "0F:11;0R:0;0S:0;0B:true";
					CommunicateToServer(pickup);
				}
				//format: 0F:11;0R:0;0S:0;0B:true

				//TODO make sequence or method that can pickup ball
				//should be room for pickup of ball here and nullifying the nearestball object
			}
			else //the list of direct balls is empty
			{
				//find nearest connection point
				if(firstConnectionTouched == false) {
					System.out.println("inside firstConnection");
					//finds closest connection point with direct path
					this.newConnectionPoint = findFirstConnectionPoint(this.robotMiddle, this.ConnectionPoints);
					//pointsOnRoute(robotMiddle, newConnectionPoint); no need to check the route as there are no balls
					// and we are only looking at points with a direct path
					//firstConnectionTouched = true;
					
					//drives to first connection
					System.out.println(robotFront.toString() + " : " + robotMiddle + " : " + newConnectionPoint);
					String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
					String a = keyb.next();
					if(counter == 0) {
						robotFront = new PointInGrid(4,15);
						findElementsInGrid();
						counter++;
					}
					else if(counter == 1) { 
						robotFront = new PointInGrid(2, 17); robotMiddle = newConnectionPoint;
						findElementsInGrid();
						counter++;
						firstConnectionTouched = true;
//						Balls.remove(Balls.lastIndexOf(nearestBall));
//						String pickup = "0F:11;0R:0;0S:0;0B:true";
//						CommunicateToServer(pickup);
						
					}
//					
						
					CommunicateToServer(commandToSend);
//					LastTouchedConnectionPoint = newConnectionPoint; //now we know where we we touched initially
					
				}
				else { //searches through connectionPoints and sets next connectionPoint appropriately
					if(branchOff == true) {
						System.out.println("going to branch off");
						//trigger if we can't get directly back
						if(checkDirectPath(pointsOnRoute(robotMiddle, branchOffPoint)) == false)
						{
							//finds the closest connection point
							newConnectionPoint = findFirstConnectionPoint(robotMiddle, ConnectionPoints);
							//drives to that point
//							LastTouchedConnectionPoint = newConnectionPoint; //this is due to resetting the robots route by getting it a totally new first connection point
							String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
							CommunicateToServer(commandToSend);
							branchOff = false;
						}
						else {
						//trigger if we can get directly back	
							String commandToSend = Calculator.getDir(robotFront, robotMiddle, branchOffPoint); //branchOffPoint : point where robot left the square track to get a ball
							CommunicateToServer(commandToSend);
							branchOff = false;
						}
					}
//					else if(LastTouchedConnectionPoint.equals(newConnectionPoint)){
					else if(robotMiddle.getX() == newConnectionPoint.getX() && robotMiddle.getY() == newConnectionPoint.getY()) {
						//all this gets triggered if the robot has reached the newConnection point
						int connPointIndexCount = 0;
						for (PointInGrid point : ConnectionPoints) {
							
//							if(LastTouchedConnectionPoint.getX() == point.getX() && LastTouchedConnectionPoint.getY() == point.getY()){
							if(robotMiddle.getX() == point.getX() && robotMiddle.getY() == point.getY()) {
								if(connPointIndexCount == 0) newConnectionPoint = ConnectionPoints.get(2); //from top left to bottom left
								if(connPointIndexCount == 1) newConnectionPoint = ConnectionPoints.get(0); //from top right to top left
								if(connPointIndexCount == 2) newConnectionPoint = ConnectionPoints.get(3); //from bottom left to bottom right
								if(connPointIndexCount == 3) newConnectionPoint = ConnectionPoints.get(1); //from bottom right to top right							
							}
							connPointIndexCount++; //increment after first if statement as we now move on to next index
						}
						//don't drive right away as we need to check for closest ball we can't reach.
						//to enforce our 90 degree angle rule while on path between two connPoints.
					}
//					else if(!LastTouchedConnectionPoint.equals(newConnectionPoint)) {
					else if(!(robotMiddle.getX() == newConnectionPoint.getX() && robotMiddle.getY() == newConnectionPoint.getY())) {						//gets triggered if the robot hasn't touched the newConnectionPoint yet.
						//we are currently either at a point or somewhere along a line bewtween
						//two connection points
						
						//since there are no direct balls we just find the nearest ball
						nearestBall = findNearestBall(robotMiddle, Balls);
						
						//we find the route between the robot and the newConnectionPoint
						// and check if we are able to hit the next ball with an angle
						//we create a branchOffPoint (we want to return to this point incase there are no more balls to pickup directly
						branchOffPoint = CheckPickupAngleOnRoute(robotMiddle, null, nearestBall, pointsOnRoute(robotMiddle, newConnectionPoint));
						
						//drives to the branch off point on the path between two locations
						String commandToSend = Calculator.getDir(robotFront, robotMiddle, branchOffPoint);
						if (counter == 2) {
							robotFront = new PointInGrid(10,15);
							findElementsInGrid();
							counter++;
						}
						else if (counter == 3) {
							robotFront = newConnectionPoint; robotMiddle = new PointInGrid(3,16);
							findElementsInGrid();
							counter = 0;
						}
						CommunicateToServer(commandToSend);
						branchOff = true;						
						////HERE SHOULD ALTER variable that allows for comm with robot////
					
						//TODO
						//Room for pickup of ball here
						//should set ballretrievedvariable here as well
					}
				}
			}
		}
		//}
		////SIMULATION END////
		/*
		this.programStillRunning = true;
		this.firstConnection = findFirstConnectionPoint(this.robotMiddle, this.ConnectionPoints);
		
		
		while (this.programStillRunning) {
			if (!firstConnectionFound) {
			Calculator.getDir(this.robotFront, this.robotMiddle, this.firstConnection);
			
			}
		}*/
	}
	
	/***
	 * finds all points between to points
	 * @param pos : robotMiddle
	 * @param dest : Destination point
	 * @return : List of PointInGrid: Containing all points between two points
	 */
	public List<PointInGrid> pointsOnRoute(PointInGrid robotMiddle, PointInGrid dest) {
		
		coordsOnPath = new ArrayList<PointInGrid>();
		
		double Slope = (dest.getX() - robotMiddle.getX()) / (dest.getY() - robotMiddle.getY());
		double Intercept = robotMiddle.getX() - Slope * robotMiddle.getY();
		
		
		//Needs fixing
		if(robotMiddle.getX() <= dest.getX() && Math.abs(robotMiddle.getY()-dest.getY()) < Math.abs(robotMiddle.getX()-dest.getX())) checkpoint = 4;
		if (robotMiddle.getY() >= dest.getY() && Math.abs(robotMiddle.getY()-dest.getY()) >= Math.abs(robotMiddle.getX()-dest.getX())) checkpoint = 3;
		if (robotMiddle.getX() >= dest.getX() && Math.abs(robotMiddle.getY()-dest.getY()) < Math.abs(robotMiddle.getX()-dest.getX())) checkpoint = 2;
		if (robotMiddle.getY() <= dest.getY() && Math.abs(robotMiddle.getY()-dest.getY()) >= Math.abs(robotMiddle.getX()-dest.getX())) checkpoint = 1;

		
		//TODO SOMEHOW TRIGGER checkpoint case 1,2,3 and 4? What if the robot is not on one of those?
		//ikke initialiseret nogle steder, så altid ende i default?
		//case 1 and 2 is deliberately < and > as case 3 and 4 handles <= and >=.
		switch(checkpoint) {
			case 1: //runs while start y is less than end y.
					//bottom to top (both sides)
				for (int y = (int) robotMiddle.getY(); y < (int) dest.getY(); y++) {
					int x = (int) (Slope * y + Intercept);
					coordsOnPath.add(new PointInGrid(x, y));
				}
				break;
			case 2: //runs while start x is less than end x.
					//right to left (both lower and upper side)
				//was ...; x <= (int)....
				for (int x = (int) robotMiddle.getX(); x > (int) dest.getX(); x--) {
					int y = (int) ((x-Intercept)/Slope);
					coordsOnPath.add(new PointInGrid(x, y));
				}
				break;
			case 3: //runs while start y is greater than end y
					//top to bottom (both sides)
				for (int y = (int) robotMiddle.getY(); y >= (int) dest.getY(); y--) {
					int x = (int) (Slope * y + Intercept);
					coordsOnPath.add(new PointInGrid(x, y));
				}
				break;
			case 4: //runs while start x is less than end x
					//left to right (both lower and upper side)
				for (int x = (int) robotMiddle.getX(); x <= (int) dest.getX(); x++) {
					int y = (int) ((x-Intercept)/Slope);
					coordsOnPath.add(new PointInGrid(x, y));
				}
				break;
			default: //purpose? will it ever get triggered?
				coordsOnPath = pointsOnRoute(robotMiddle, dest);
				break;
		}
		
		
		
		
		return coordsOnPath;
	}
	
	/***
	 * Evaluates all points in route.
	 * to see if there is an angle on the route towards the nearest ball from
	 * the side within the 85-95 degree angle.
	 * returns a null PointInGrid if there is an obstacle (barrier or cross) 
	 * between the robot and the ball
	 * @param Robot : RobotMiddle
	 * @param nextPoint : a point on path, should be null atm
	 * @param nearestBall : the closest ball
	 * @param pointsOnPath : The path between two points in coords
	 */
	//TODO FIX IMPLEMENTATION (remove nextPoint and use the correct list in actual impl)
	@Override
	public PointInGrid CheckPickupAngleOnRoute(PointInGrid Robot, PointInGrid nextPoint, PointInGrid nearestBall, List<PointInGrid> pointsOnPath) {
		
		PointInGrid Point = null;
		
		for (PointInGrid p : pointsOnPath) {
			double angle = Calculator.calc_Angle(Robot, p, nearestBall);
			
			if (angle >= 85 && angle <= 95) {
				Point = p;
				break;
			}
		}
		return Point;
	}

	/**
	 * searches through all balls in list and reevalutes if one is closer than the other
	 * @return PointInGrid: point of the closest ball (no matter its position)
	 * @param Robot : middle of robot 
	 */
	@Override
	public PointInGrid findNearestBall(PointInGrid Robot, List<PointInGrid> BallPoints) {
		double dist = 10000;
		PointInGrid closestPoint = null;
		
		for (PointInGrid Point : BallPoints) {
			if (Calculator.calc_Dist(Robot, Point) < dist) {
				dist = Calculator.calc_Dist(Robot, Point);
				closestPoint = Point;
			}		
		}
		
		return closestPoint;
	}

	/**
	 * Should be method for sending a string to robot
	 * @param Robot: The middle of the robot
	 * @param conPoint: the front of the robot
	 * @param nextCornor : ??
	 * @param nearestBall : a ball to consider in the method
	 */
	//TODO CHANGE IMPLEMENTATION
	@Override
	public void Drive(PointInGrid conPoint, PointInGrid Robot, PointInGrid nextCornor, PointInGrid nearestBall) {		
		
		//this gets triggered if the EvalRoute could 
		//not find a point in which there was
		//an appropriate angle towards the ball without an obstacle in between
		if (CheckPickupAngleOnRoute(Robot, nextCornor, nearestBall, null) == null)
		{
			//will compile the string to send to the robot so that the robot can drive
			//to the next corner
			String commandToSend = Calculator.getDir(conPoint, Robot, nextCornor);
			CommunicateToServer(commandToSend);
		}
		//if there was a succesfull point with angle then
		//create the route 
		else
		{
			String commandToSend = Calculator.getDir(conPoint, Robot, CheckPickupAngleOnRoute(Robot, nextCornor, nearestBall,null));
			CommunicateToServer(commandToSend);
			//Calculator.getDir(conPoint, Robot, EvalRoute(Robot, nextCornor, findNearestBall(Robot, BallPoints)));	
		}
	}
	
	/**
	 * not used atm, for threading
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * This creates a simulated grid
	 * it contains, barriers, balls, robot and connection points
	 */
	///////////USED FOR SIMULATION GRID///////////
	public void CreateGrid()
	{	
		int rows = 20;
		int columns = 20;
		/**
		 * This for loop construction simulates walls in the grid
		 */
		//first for loop iterates downwards through rows
		//second for loop iterates to the right through columns
		for (int a = 0; a <= rows; a++) {
			for (int b = 0; b < columns; b++) {
				if((a == 1 || a == 18) && b >= 1 && b <= 18) SimulatedGrid[a][b] = 1;
				if((b == 1 || b == 18) && a >= 1 && a <= 18) SimulatedGrid[a][b] = 1;
				if(a == 9 && b >= 6 && b <= 13) SimulatedGrid[a][b] = 1;
				if(b == 9 && a >= 6 && a <= 13) SimulatedGrid[a][b] = 1;
			}
		}
		
		/**
		 * This inserts the four connection points into the map
		 */
			for (int a = 0; a < rows; a++) {
				for (int b = 0; b < columns; b++) {
					if((a == 3 && b == 3)) SimulatedGrid[a][b] = 5;
					if((b == 3 && a == 16)) SimulatedGrid[a][b] = 6;
					if((a == 16 && b == 16)) SimulatedGrid[a][b] = 7;
					if((b == 16 && a == 3)) SimulatedGrid[a][b] = 8;
				}
			}
		
		/**
		 * This line inserts the robot into the grid
		 */
			int RobotFront = 3;
			int RobotMid = 2;
			SimulatedGrid[5][14] = RobotMid;
			SimulatedGrid[6][15] = RobotFront;
			
		/**
		 * This inserts 6 balls into the system, whereas one is outside the main barrier.
		 */
			int ball = 4;
			//SimulatedGrid[12][5] = ball;
			SimulatedGrid[10][10] = ball;
//			SimulatedGrid[12][16] = ball;
//			SimulatedGrid[3][6] = ball;
//			SimulatedGrid[7][15] = ball;
			SimulatedGrid[19][19] = ball;
	}

	
	/**
	 * Finds elements in grid (used for simulation) 
	 * In the actual implementation, the image recog will send the grid info in lists
	 */
	/////////USED FOR SIMULATION GRID/////////
	public void findElementsInGrid()
	{
		int rows = 20;
		int columns = 20;

		for (int a = 0; a < rows; a++) {
			for (int b = 0; b < columns; b++) {
				if(SimulatedGrid[a][b] == 2) robotMiddle = new PointInGrid(a,b);
				if(SimulatedGrid[a][b] == 3) robotFront = new PointInGrid(a,b);
				if(SimulatedGrid[a][b] == 4) {
					PointInGrid tmpBall = new PointInGrid(a, b);
					Balls.add(tmpBall);
				}
				if(SimulatedGrid[a][b] == 5 || SimulatedGrid[a][b] == 6 ||
						   SimulatedGrid[a][b] == 7 ||SimulatedGrid[a][b] == 8)
				{
					PointInGrid tmpConnPoint = new PointInGrid(a, b);
					ConnectionPoints.add(tmpConnPoint);
				}
			}
		}
	}
	
	/**
	 * checks the vertical and horizontal area of each point in a list
	 * @param coordsOnPathLocal : all points between two points
	 * @return whether or not the path is clear
	 */
	////////CURRENTLY NOT USED//////////
	public int SearchBroaderPath(List<PointInGrid> coordsOnPathLocal)
	{
		for (PointInGrid point : coordsOnPathLocal) {
			for (int i = (int)point.getY(); i <= (int)point.getY()+2 ; i++)
			{
				if(SimulatedGrid[(int) point.getX()][i] == ballvalue) return ballvalue;
				if(SimulatedGrid[(int) point.getX()][i] == OBSTACLE) return OBSTACLE;
			}
			for (int i = (int)point.getY(); i >= (int)point.getY()-2; i--) 
			{
				if(SimulatedGrid[(int) point.getX()][i] == ballvalue) return ballvalue;
				if(SimulatedGrid[(int) point.getX()][i] == OBSTACLE) return OBSTACLE;
			}
			for (int i = (int)point.getX(); i <= (int)point.getX()+2; i++) 
			{
				if(SimulatedGrid[(int) point.getY()][i] == ballvalue) return ballvalue;
				if(SimulatedGrid[(int) point.getY()][i] == OBSTACLE) return OBSTACLE;
			}
			for (int i = (int)point.getX(); i >= (int)point.getX()-2; i--) 
			{
				if(SimulatedGrid[(int) point.getY()][i] == ballvalue) return ballvalue;
				if(SimulatedGrid[(int) point.getY()][i] == OBSTACLE) return OBSTACLE;
			}
		}
		return 0;
	}

	/**
	 * Checks all points to see if an obstacle occurs on the route.
	 * @param directpath : List of coords on direct path computed by pointsOnRoute()
	 * @return : true if no obstacles or false if obstacles
	 */
	public boolean checkDirectPath(List<PointInGrid> directpath)
	{
		boolean bøv = true;
		for (PointInGrid p : directpath)
		{
			//checks if the simulatedGrid is initialized or if the actual grid is.
			if(SimulatedGrid != null)
			{
				if(SimulatedGrid[(int)p.getX()][(int)p.getY()] == OBSTACLE)
				{
					bøv = false;
				}
			}
			
			if(ImageGrid != null)
			{
				if(ImageGrid[(int) p.getX()][(int) p.getY()] == OBSTACLE)
				{
					bøv = false;
				}
			}
			
		}
		return bøv;
	}

 
	/**
	 * Checks to see if the robot can get a 
	 * 90 degree angle by rotating around itself that points towards the nearest ball
	 * @param robotMid
	 * @param robotFront
	 * @param nearestBall
	 * @return
	 */
	//////////CURRENTLY NOT USED//////////
	public PointInGrid CheckPickupAngleSelfRotate(PointInGrid robotMid, PointInGrid robotFront, PointInGrid nearestBall)
	{
		List<PointInGrid> RobotPerimeterPoints = new ArrayList<PointInGrid>();
		
		//gets the robot perimeter points
		RobotPerimeterPoints = GetRobotPerimeter(robotMid.getX(), robotMid.getY(), Calculator.calc_Dist(robotMid, robotFront));
		
		//checks for angle where each robotPerimeterPoint is the nose of the robot in a full circle
		for (PointInGrid robotFrontPoint : RobotPerimeterPoints) {
			double angle = Calculator.calc_Angle(robotFrontPoint, robotMid, nearestBall);
			if (angle >= 85 && angle <= 95) {
				return robotFrontPoint;
			}
		}
		//this will never be triggered
		return null;
	}
	
	//x is robotmiddle.getX(), y is robotmiddle.getY().
	//r is from dist form between robotMiddle and robotFront.
	/**
	 * finds the robots circumference were it to do a 360 rotation
	 * @param RmidX : the robot middle x coord
	 * @param RmidY : the robot middle y coord
	 * @param robotRadius : the distance between robot middle and robot front
	 * @return
	 */
	public static List<PointInGrid> GetRobotPerimeter(double RmidX, double RmidY, double robotRadius)
	{
		List<PointInGrid> CirclePoints = new ArrayList<PointInGrid>();
		    
		double PI = 3.1415926535;
	    double i, angle, x1, y1;

		    //iterates through all 360 angles
		    for (i = 0; i < 360; i += 1) {
		        angle = i;
		        //finds coordinates on sin and cos with radius
		        x1 = robotRadius * Math.cos(angle * PI / 180);
		        y1 = robotRadius * Math.sin(angle * PI / 180);
		        //
		        int ElX = (int) (RmidX + x1);
		        int ElY = (int) (RmidY + y1);
		        //SimulatedGrid[ElX][ElY] = 1;
		        //setElementColor(color);
		        CirclePoints.add(new PointInGrid(ElX,ElY));
		    }
		    return CirclePoints; 
		}
	
	/**
	 * Finds balls with direct path (no obstacles inbetween)
	 * @param robotMiddle
	 * @param balls
	 * @return a list of PointInGrid
	 */
	public List<PointInGrid> BallsWithDirectPath(PointInGrid robotMiddle, List<PointInGrid> balls)
	{
		List<PointInGrid> ballsWithDirectPath = new ArrayList<PointInGrid>();
		//for each ball, check its path and put it into a list if no obstacles
		for (PointInGrid ballPoint : balls) {
			if(checkDirectPath(pointsOnRoute(robotMiddle, ballPoint))) //gets route, checks route
			{
				for (PointInGrid p : coordsOnPath)
					System.out.println("GETX: " + p.getX() + ", GETY: " + p.getY() + ", ALL: " + SimulatedGrid[(int) p.getX()][(int) p.getY()]);
				System.out.println("BOOL: " + checkDirectPath(pointsOnRoute(robotMiddle, ballPoint)));
				keyb.next();
				ballsWithDirectPath.add(ballPoint);
			}
		}
		return ballsWithDirectPath;
	}

	public void CommunicateToServer(String command)
	{
		//RC.SendCommandString(command);
	}
}