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

import javax.swing.plaf.synth.Region;

import RobotControl.Billedbehandling;
//import RobotControl.Billedbehandling_27032019;
import RobotControl.RemoteCarClient;
//import RouteCalculator.PointInGrid;

public class RouteLogic implements IRouteLogic, Runnable {
	
	////SIMULATIONGRID AND SIMULATIONBALLS////
	private int[][] SimulatedGrid = new int[20][20];
	private List<Point> listofBallCoords = new ArrayList<Point>();
	
	//NOT SIMULATION VARIABLES//
	private int ballvalue = 4;
	private int checkpoint;
	private List<Point> coordsOnPath;
	private Point robotMiddle, robotFront;
	private Point safeHazardPoint, smallGoalSafeSpot, smallGoal, nextCornerToNavigateTo;
	private Point firstConnection, LastTouchedConnectionPoint, newConnectionPoint, branchOffPoint;
	private List<Point> Balls, ConnectionPoints, safeBalls, dangerBalls, dangerPickupPoints; 
	private int[][] ImageGrid;
	private final int OBSTACLE = 1, HAZARD = 20;
	boolean firstConnectionFound,firstConnectionTouched, programStillRunning, branchOff, readyToNavigateToAHazardPoint, pickupBall;
	private boolean returnToPrevHazardPoint, unloadBalls, SPINWIN, NavigateToNextConnectionPoint;
	private RemoteCarClient RC;
	//private Billedbehandling_27032019 ImageRec;
	private Billedbehandling ImageRec;
	private RouteCalculatorInterface Calculator;
	Scanner keyb = new Scanner(System.in); //Hvad er det her???
	
	public RouteLogic() {
		this.Calculator = new RouteCalculator();
		this.RC = Main.RC;
		//this.ImageRec = Main.ImageRec;
	}
	public RouteLogic(Billedbehandling BB)
	{
		this.ImageRec = BB;
		this.Calculator = new RouteCalculator();
		this.RC = Main.RC;
	}
	
	/**
	 * Constructor for the class
	 * @param robotMiddle rotaionCenter on robot
	 * @param robotFront Point right in front of robot
	 * @param Balls Locations of the balls on the track
	 * @param ConnectionPoints Four connectionPoints posing for the robot's overall path
	 * @param ImageGrid 2D array that imitates the entire track
	 */
	public RouteLogic(Point robotMiddle, Point robotFront, List<Point> Balls, List<Point> ConnectionPoints, int[][] ImageGrid) 
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
	private Point findFirstConnectionPoint(Point robotMiddle, List<Point> ConnectionPoints) {
		double dist = 10000;
		Point closestPoint = null;
		
		List<Point> connPointWithDirectPath = new ArrayList<Point>();
		//checks if the points have a direct path
		for (Point connPoint : ConnectionPoints) {
			if(checkDirectPath(pointsOnRoute(robotMiddle, connPoint)) == true) {
				connPointWithDirectPath.add(connPoint);
			}
		}

		//finds the closest of those with direct path
		for (Point connPoint : connPointWithDirectPath) {
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
		this.ConnectionPoints = new ArrayList<Point>();
		this.Balls = new ArrayList<Point>();
		//CommunicateToServer("0F:1;0G:0;0S:300;LR:200;RR:200;0B:true;");
		
		////SIMULATION START////
		CreateGrid(); //creates a artificial grid to use in simulation
		findElementsInGrid(); //finds balls, robot points and connectionpoints
		
		//TODO take picture and get initial elements
		
		this.programStillRunning = true;
		int counter =  0;
		while (this.programStillRunning) {
			
			//if (RC.GetSendingStatus() == false) { //if the sending status returned is false	
			Point nearestBall;
			//TODO take picture and get elements
			
			//find all balls with a direct path
			List<Point> ballsWithDirectPathFromRobot = BallsWithDirectPath(robotMiddle, Balls);
			
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
					robotFront = new Point(4,5);
					findElementsInGrid();
					counter++;
				}
				else if(counter == 1) { 
					robotFront = new Point(3,6); robotMiddle = new Point(4,5); 
					Balls.remove(Balls.lastIndexOf(nearestBall));
					findElementsInGrid();
					String pickup = "0F:11;0R:0;0S:0;0B:true";
					CommunicateToServer(pickup);
				}
				//format: 0F:11;0R:0;0S:0;0B:true

				//TODO make sequence or method that can pickup ball
				//should be room for pickup of ball here and nullifying the nearestball object
			}
			else//the list of direct balls is empty
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
					
					//reset counter for this case
					counter = 0;
					if(counter == 0) {
						robotFront = new Point(4,15);
						findElementsInGrid();
						counter++;
					}
					else if(counter == 1) { 
						robotFront = new Point(2, 17); robotMiddle = newConnectionPoint;
						findElementsInGrid();
						counter++;
						firstConnectionTouched = true;
//						Balls.remove(Balls.lastIndexOf(nearestBall));
//						String pickup = "0F:11;0R:0;0S:0;0B:true";
//						CommunicateToServer(pickup);	
					}
					CommunicateToServer(commandToSend);
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
					else if(robotMiddle.getX() == newConnectionPoint.getX() && robotMiddle.getY() == newConnectionPoint.getY()) {
						//all this gets triggered if the robot has reached the newConnection point
						int connPointIndexCount = 0;
						for (Point point : ConnectionPoints) {
							
							if(robotMiddle.getX() == point.getX() && robotMiddle.getY() == point.getY()) {
								if(connPointIndexCount == 0) newConnectionPoint = ConnectionPoints.get(2); //from top left to bottom left
								if(connPointIndexCount == 1) newConnectionPoint = ConnectionPoints.get(0); //from top right to top left
								if(connPointIndexCount == 2) newConnectionPoint = ConnectionPoints.get(3); //from bottom left to bottom right
								if(connPointIndexCount == 3) newConnectionPoint = ConnectionPoints.get(1); //from bottom right to top right							
							}
							connPointIndexCount++; //increment after first if statement as we now move on to next index
						}
					}
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
							robotFront = new Point(10,15);
							findElementsInGrid();
							counter++;
						}
						else if (counter == 3) {
							robotFront = newConnectionPoint; robotMiddle = new Point(3,16);
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
	
	//an implementation that picks up safe balls first, then dangerous balls
	//using two different rule sets.
	public void runningTwo()
	{
		System.out.println("MADE IT INTO RUNNINGTWO");
		ImageGrid = new int[1080][1920];
		//INITIALIZE FOR TEST DATA
		//WE NEED CONNECTION POINTS
		//SAFE BALLS
		//DANGER BALLS
		//DANGER BALLS POINTS
		//GOAL SPOT
		//SAFE SPOT TO LAND BEFORE HANDING IN BALLS TO GOAL (GOAL SPOT)

		
		
//		this.ConnectionPoints = new ArrayList<Point>();
//		this.Balls = new ArrayList<Point>();
//		
//		CreateGrid(); //creates a artificial grid to use in simulation
//		findElementsInGrid(); //finds balls, robot points and connectionpoints
//		
		//TODO take picture and get initial elements
		
		this.programStillRunning = true;
		int counter =  0;
		Point nearestBall;
		while (this.programStillRunning) {
			//if (RC.GetSendingStatus() == false) { //if the sending status returned is false	
				
			//Get info from imageRec.
			ImageRec.runImageRec();
			GetImageInfo();
			
			while(RC.IsRobotExecuting() == true) {}
	
						
			List<Point> ballsWithDirectPathFromRobot = BallsWithDirectPathObstacleHazard(robotMiddle, safeBalls);//find all balls with a direct path
			if(SPINWIN == true)
			{
				CommunicateToServer("0F:3;0R:1500;0S:300;0B:true");
				//this stops the while loop
				this.programStillRunning = false;
			}
			else if(ballsWithDirectPathFromRobot.size() != 0)
			{
				//finds the safest ball and communicates to the server
				//counter = 
				NearestSafeBallPickupAlgorithm(ballsWithDirectPathFromRobot, counter);
				
			}
//			else if(safeBalls.isEmpty() && !dangerBalls.isEmpty())//no more safe balls and still dangerous balls
//			{
//				HazardBallPickupAlgorithm();
//			}
			else if(safeBalls.isEmpty())
			{
				HeadForGoalAndUnload();
			}
//			else if(safeBalls.isEmpty() && dangerBalls.isEmpty()) //go to goal
//			{
//				//check for direct path to goal safe spot (not through hazard zone)
//				HeadForGoalAndUnload();
//			}
			//We are at a point and can't reach any safe balls
			else if(checkIfCoordsNear(robotMiddle, newConnectionPoint)) { //sets the new connectionPoint
				//all this gets triggered if the robot has reached the newConnection point
				newConnectionPoint = nextConnPoint(robotMiddle, ConnectionPoints);
			}
			else { //we are not at a point and there is safe balls but they
				   // can't get picked up
				   // finds closest connectionPoint and drives to it.
				newConnectionPoint = findFirstConnectionPoint(robotMiddle, ConnectionPoints);
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
				CommunicateToServer(commandToSend);
			}
		}
	}
	
	
	//GETS INFO FROM imagerecognition 
	public void GetImageInfo()
	{	
		robotMiddle = ConvertPoint(ImageRec.robotGreenMarker);
		robotFront = ConvertPoint(ImageRec.robotBlueMarker);
		safeBalls = ConvertPoint(ImageRec.listOfBallCoordinates);
//ImageGric = Con
//		robotFront = ImageRec.robotBlueMarker;
//		safeBalls = ImageRec.listOfBallCoordinates;
		
//		ConnectionPoints = ImageRec.GetConnectionPoints();
//		dangerBalls = ImageRec.GetDangerBalls();
//		dangerPickupPoints = ImageRec.GetHazardPoints();
//		smallGoal = ImageRec.GetSmallGoal();
//		smallGoalSafeSpot = ImageRec.GetSmallGoalSafeSpot();
//		ImageGrid = ImageRec.GetTotalGrid();

	}
	/***
	 * finds all points between to points
	 * @param pos : robotMiddle
	 * @param dest : Destination point
	 * @return : List of Point: Containing all points between two points
	 */
	public List<Point> pointsOnRoute(Point robotMiddle, Point dest) {
		
		coordsOnPath = new ArrayList<Point>();
		
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
					coordsOnPath.add(new Point(x, y));
				}
				break;
			case 2: //runs while start x is less than end x.
					//right to left (both lower and upper side)
				//was ...; x <= (int)....
				for (int x = (int) robotMiddle.getX(); x > (int) dest.getX(); x--) {
					int y = (int) ((x-Intercept)/Slope);
					coordsOnPath.add(new Point(x, y));
				}
				break;
			case 3: //runs while start y is greater than end y
					//top to bottom (both sides)
				for (int y = (int) robotMiddle.getY(); y >= (int) dest.getY(); y--) {
					int x = (int) (Slope * y + Intercept);
					coordsOnPath.add(new Point(x, y));
				}
				break;
			case 4: //runs while start x is less than end x
					//left to right (both lower and upper side)
				for (int x = (int) robotMiddle.getX(); x <= (int) dest.getX(); x++) {
					int y = (int) ((x-Intercept)/Slope);
					coordsOnPath.add(new Point(x, y));
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
	 * returns a null Point if there is an obstacle (barrier or cross) 
	 * between the robot and the ball
	 * @param Robot : RobotMiddle
	 * @param nextPoint : a point on path, should be null atm
	 * @param nearestBall : the closest ball
	 * @param pointsOnPath : The path between two points in coords
	 */
	//TODO FIX IMPLEMENTATION (remove nextPoint and use the correct list in actual impl)
	@Override
	public Point CheckPickupAngleOnRoute(Point Robot, Point nextPoint, Point nearestBall, List<Point> pointsOnPath) {
		
		Point Point = null;
		
		for (Point p : pointsOnPath) {
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
	 * @return Point: point of the closest ball (no matter its position)
	 * @param Robot : middle of robot 
	 */
	@Override
	public Point findNearestBall(Point Robot, List<Point> BallPoints) {
		double dist = 10000;
		Point closestPoint = null;
		
		for (Point Point : BallPoints) {
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
	public void Drive(Point conPoint, Point Robot, Point nextCornor, Point nearestBall) {		
		
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
				if(SimulatedGrid[a][b] == 2) robotMiddle = new Point(a,b);
				if(SimulatedGrid[a][b] == 3) robotFront = new Point(a,b);
				if(SimulatedGrid[a][b] == 4) {
					Point tmpBall = new Point(a, b);
					Balls.add(tmpBall);
				}
				if(SimulatedGrid[a][b] == 5 || SimulatedGrid[a][b] == 6 ||
						   SimulatedGrid[a][b] == 7 ||SimulatedGrid[a][b] == 8)
				{
					Point tmpConnPoint = new Point(a, b);
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
	public int SearchBroaderPath(List<Point> coordsOnPathLocal)
	{
		for (Point point : coordsOnPathLocal) {
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
	public boolean checkDirectPath(List<Point> directpath)
	{
		boolean bøv = true;
		for (Point p : directpath)
		{
			if(ImageGrid != null)
			{
				if(ImageGrid[(int) p.getX()][(int) p.getY()] == OBSTACLE)
				{
					bøv = false;
				}
			}
			//checks if the simulatedGrid is initialized or if the actual grid is.
			else if(SimulatedGrid != null)
			{
				if(SimulatedGrid[(int)p.getX()][(int)p.getY()] == OBSTACLE)
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
	public Point CheckPickupAngleSelfRotate(Point robotMid, Point robotFront, Point nearestBall)
	{
		List<Point> RobotPerimeterPoints = new ArrayList<Point>();
		
		//gets the robot perimeter points
		RobotPerimeterPoints = GetRobotPerimeter(robotMid.getX(), robotMid.getY(), Calculator.calc_Dist(robotMid, robotFront));
		
		//checks for angle where each robotPerimeterPoint is the nose of the robot in a full circle
		for (Point robotFrontPoint : RobotPerimeterPoints) {
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
	public static List<Point> GetRobotPerimeter(double RmidX, double RmidY, double robotRadius)
	{
		List<Point> CirclePoints = new ArrayList<Point>();
		    
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
		        CirclePoints.add(new Point(ElX,ElY));
		    }
		    return CirclePoints; 
		}
	
	/**
	 * Finds balls with direct path (no obstacles inbetween)
	 * @param robotMiddle
	 * @param balls
	 * @return a list of Point
	 */
	public List<Point> BallsWithDirectPath(Point robotMiddle, List<Point> balls)
	{
		List<Point> ballsWithDirectPath = new ArrayList<Point>();
		//for each ball, check its path and put it into a list if no obstacles
		for (Point ballPoint : balls) {
			if(checkDirectPath(pointsOnRoute(robotMiddle, ballPoint))) //gets route, checks route
			{
				for (Point p : coordsOnPath)
					System.out.println("GETX: " + p.getX() + ", GETY: " + p.getY() + ", ALL: " + SimulatedGrid[(int) p.getX()][(int) p.getY()]);
				System.out.println("BOOL: " + checkDirectPath(pointsOnRoute(robotMiddle, ballPoint)));
				keyb.next();
				ballsWithDirectPath.add(ballPoint);
			}
		}
		return ballsWithDirectPath;
	}

	//sends a string to the server
	public void CommunicateToServer(String command)
	{
		RC.SendCommandString(command);
	}
	
	//checks if two Points coords are equals
	public boolean checkIfCoordsEqual(Point robotMiddle, Point dest)
	{
		if(robotMiddle.getX() == dest.getX() && robotMiddle.getY() == dest.getY()) return true;				
		
		return false;
	}
	//checks if two Points coords are equals
	public boolean checkIfCoordsNear(Point robotMiddle, Point dest)
	{
		if(robotMiddle.getX() < dest.getX()+3 && robotMiddle.getX() > dest.getX()-3 &&
		 robotMiddle.getY() < dest.getY()+3 && robotMiddle.getY() > dest.getY()-3
				) return true;						
		return false;
	}
	
	/**
	 * finds the next connectionPoint as long as the robot is on one
	 */
	public Point nextConnPoint(Point robotMiddle, List<Point> ConnectionPoints)
	{
		int connPointIndexCount = 0;
		for (Point point : ConnectionPoints) {
			
			if(robotMiddle.getX() == point.getX() && robotMiddle.getY() == point.getY()) {
				if(connPointIndexCount == 0) return ConnectionPoints.get(2); //from top left to bottom left
				if(connPointIndexCount == 1) return ConnectionPoints.get(0); //from top right to top left
				if(connPointIndexCount == 2) return ConnectionPoints.get(3); //from bottom left to bottom right
				if(connPointIndexCount == 3) return ConnectionPoints.get(1); //from bottom right to top right							
			}	
		}
		return null;	
	}
	
	/**
	 * drives to closest connectionPoint
	 * checks if it can find a hazard point it can navigate to
	 * navigates to it, navigates to closest ball thereafter
	 * returns to hazardpoint
	 * returns to closest connectionPoint
	 */
	public void HazardBallPickupAlgorithm()
	{
		if(readyToNavigateToAHazardPoint == true) //we are at a connPoint ready to get some dangerballs
		{	//finds safe pickup point - navigate to it if possible
			safeHazardPoint = findNearestBall(robotMiddle, dangerPickupPoints);
			//not using directpathHazardCheck because the hazard spot can be in the hazard zone.
			boolean allowTrip = checkDirectPath(pointsOnRoute(robotMiddle, safeHazardPoint));
				if(allowTrip == true) //will be adjusted automatically next time method is called
				{
					String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
					CommunicateToServer(commandToSend);
					if(checkIfCoordsEqual(robotMiddle, safeHazardPoint)) {
						readyToNavigateToAHazardPoint = false;
						pickupBall = true;
					}
				}
				else { //find next connectionPoint on route
					nextCornerToNavigateTo = nextConnPoint(robotMiddle, ConnectionPoints);
					readyToNavigateToAHazardPoint = false;
					NavigateToNextConnectionPoint = true;
				}
		}
		else if(pickupBall == true) //if we have reached a hazard point and we can pickup the ball
		{
			newConnectionPoint = findNearestBall(robotMiddle, dangerBalls);
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
			CommunicateToServer(commandToSend);
			if(checkIfCoordsEqual(robotMiddle, newConnectionPoint)) {
				pickupBall = false;
				returnToPrevHazardPoint = true;
			}
			CommunicateToServerPickup();
		}
		else if(returnToPrevHazardPoint == true){ //if we have returned to our safe hazard point
			newConnectionPoint = findFirstConnectionPoint(robotMiddle, ConnectionPoints); String commandToSend = Calculator.getDir(robotFront, robotMiddle, safeHazardPoint);
			CommunicateToServer(commandToSend);
			if(checkIfCoordsEqual(robotMiddle, newConnectionPoint)) returnToPrevHazardPoint = false;
		}
		else if(NavigateToNextConnectionPoint == true)
		{
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, nextCornerToNavigateTo);
			CommunicateToServer(commandToSend);
			if(checkIfCoordsEqual(robotMiddle, nextCornerToNavigateTo) == true)
			{
				readyToNavigateToAHazardPoint = true;
				NavigateToNextConnectionPoint = false;
			}
		}
		else{ //constantly attempts to navigate to the closest connPoint
			//gets conn point and calculates string to robot 
			newConnectionPoint = findFirstConnectionPoint(robotMiddle, ConnectionPoints); String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
			CommunicateToServer(commandToSend);
			if(checkIfCoordsEqual(robotMiddle, newConnectionPoint))
			{
				readyToNavigateToAHazardPoint = true;
			}
		}
	}
	
	/**
	 * sends the pickup command to the robot
	 */
	public void CommunicateToServerPickup()
	{
		CommunicateToServer("0F:11;0R:0;0S:0;0B:true;");
	}

	//picks up the nearest ball
	public int NearestSafeBallPickupAlgorithm(List<Point> safeBalls, int counter)
	{		
		//if the list isn't empty
		Point nearestBall = findNearestBall(robotFront,safeBalls);
		//Point nearestBall = findNearestBall(robotMiddle, safeBalls);
		while(!checkIfCoordsNear(robotFront, nearestBall))
		{
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, nearestBall);
			//keyb.next();
			CommunicateToServer(commandToSend);
			ImageRec.runImageRec();
			
			GetImageInfo();
		}
		CommunicateToServerPickup();
		
		//Scenario to get close ball
/*		if(counter == 0) {
			robotFront = new Point(4,5);
			findElementsInGrid();
			counter++;
		}
		else if(counter == 1) { 
			robotFront = new Point(3,6); robotMiddle = new Point(4,5); 
			safeBalls.remove(safeBalls.lastIndexOf(nearestBall));
			findElementsInGrid();
			CommunicateToServerPickup();
		}*/
		return counter; //the return is used for testing
	}
	
	//heads for goal safe spot and unloads
	public void HeadForGoalAndUnload()
	{
		if(unloadBalls == true)
		{
			CommunicateToServer("0F:12;0R:0;0S:0;0B:true;");
			unloadBalls = false;
			SPINWIN = true;
		}
		else {
			boolean allowTrip = checkDirectPathObstacleHazard(pointsOnRoute(robotMiddle, smallGoalSafeSpot));
			if(allowTrip == true)
			{
				newConnectionPoint = smallGoalSafeSpot;
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
				CommunicateToServer(commandToSend);
				if(checkIfCoordsEqual(robotMiddle, newConnectionPoint))
				{
					allowTrip = false;
					unloadBalls = true;
				}
			}
		}
	}
	
	//checks if a direct path touches hazard zones or obstacles
	public boolean checkDirectPathObstacleHazard(List<Point> directpath)
	{
		for (Point p : directpath)
		{
			if(ImageGrid != null)
			{
				if(ImageGrid[(int) p.getY()][(int) p.getX()] == OBSTACLE
				  || ImageGrid[(int) p.getY()][(int) p.getX()] == HAZARD)
				{
					return false;
				}
			}
			//checks if the simulatedGrid is initialized or if the actual grid is.
//			else if(SimulatedGrid != null)
//			{
//				if((SimulatedGrid[(int)p.getX()][(int)p.getY()] == OBSTACLE) 
//				   || SimulatedGrid[(int)p.getX()][(int)p.getY()] == HAZARD)
//				{
//					return false;
//				}
//			}
		}
		return true;
	}

	//returns a list of balls that isn't obstructed by obstacles or a hazard zone
	public List<Point> BallsWithDirectPathObstacleHazard(Point robotMiddle, List<Point> balls)
	{
		List<Point> ballsWithDirectPath = new ArrayList<Point>();
		//for each ball, check its path and put it into a list if no obstacles
		for (Point ballPoint : balls) {
			if(checkDirectPathObstacleHazard(pointsOnRoute(robotMiddle, ballPoint))) //gets route, checks route
			{
				ballsWithDirectPath.add(ballPoint);
			}
		}
		return ballsWithDirectPath;
	}

	public Point ConvertPoint(org.opencv.core.Point point)
	{
		Point p = new Point((int)point.x,(int)point.y);
		return p;
	}
	public List<Point> ConvertPoint(List<org.opencv.core.Point> pointList)
	{
		List<Point> p = new ArrayList<Point>();
		for (org.opencv.core.Point op : pointList) {
			p.add(ConvertPoint(op));
		}
		return p;
	}
}
