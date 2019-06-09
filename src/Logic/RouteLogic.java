package Logic;

import RouteCalculator.RouteCalculator;
import RouteCalculator.RouteCalculatorInterface;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import RouteCalculator.PointInGrid;

public class RouteLogic implements IRouteLogic, Runnable {
	
	////SIMULATIONGRID AND SIMULATIONBALLS////
	private int[][] SimulatedGrid = new int[20][20];
	private List<PointInGrid> listofBallCoords = new ArrayList<PointInGrid>();
	
	//NOT SIMULATION VARIABLES//
	private int ballvalue = 4;
	private int checkpoint;
	private List<PointInGrid> coordsOnPath;
	private PointInGrid robotMiddle, robotFront, firstConnection; 
	private List<PointInGrid> Balls, ConnectionPoints; 
	private int[][] ImageGrid;
	private final int OBSTACLE = 1;
	boolean firstConnectionFound, programStillRunning;
	
	private RouteCalculatorInterface Calculator;
	
	public RouteLogic() {
		this.Calculator = new RouteCalculator();
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
	 * @param robotMiddle rotaionCenter on robot
	 * @param ConnectionPoints Four connectionPoints posing for the robot's overall path
	 */
	private PointInGrid findFirstConnectionPoint(PointInGrid robotMiddle, List<PointInGrid> ConnectionPoints) {
		double dist = 10000;
		PointInGrid closestPoint = null;

		for (PointInGrid connection : ConnectionPoints) {
			if (Calculator.calc_Dist(robotMiddle, connection) < dist) {
			dist = Calculator.calc_Dist(robotMiddle, connection);
			closestPoint = connection;
			}
		}
		
		return closestPoint;
	}
	
	/**
	 * This method computes where the robot shall go
	 */
	public void running() {
	
		////SIMULATION START////
		CreateGrid(); //creates a artificial grid to use in simulation
		findElementsInGrid(); //finds balls, robot points and connectionpoints
		
		this.firstConnection = findFirstConnectionPoint(this.robotMiddle, this.ConnectionPoints);
		this.programStillRunning = true;
		
		System.out.println("x : " + this.firstConnection.getX() + "\n y : " + this.firstConnection.getY());
		
		
		while (this.programStillRunning) {
			//Vi skal finde den tætteste bold først før vi kører til first connection.
			PointInGrid Nearestball = findNearestBall(robotFront, listofBallCoords);
			//vi skal se om vi kan finde path hen til første forbindelsespunkt uden at ramme en bold
			//hvis vi kan så kører vi derhen og starter turen
			//hvis ikke samler vi den bold op som ligger i vejen og så tjekker vi igen og gentager
			//indtil vi kan komme hen til forbindelsespunktet.
			
//			EvalRoute(robotMiddle, firstConnection, nearestBall)
			
			/*WHY???? if (!firstConnectionFound) {
			Calculator.getDir(this.robotFront, this.robotMiddle, this.firstConnection);
			
			}*/
		}
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
	 * @param pos
	 * @param dest
	 * @return
	 */
	public List<PointInGrid> pointsOnRoute(PointInGrid pos, PointInGrid dest) {
		
		coordsOnPath = new ArrayList<PointInGrid>();
		
		double Slope = (dest.getX() - pos.getX()) / (dest.getY() - pos.getY());
		double Intercept = pos.getX() - Slope * pos.getY();
		
		//TODO SOMEHOW TRIGGER checkpoint case 1,2,3 and 4? What if the robot is not on one of those?
		//ikke initialiseret nogle steder, så altid ende i default?
		switch(checkpoint) {
			case 1: //runs while start y is less than end y.
					//bottom to top (both sides)
				for (int y = (int) pos.getY(); y <= (int) dest.getY(); y++) {
					int x = (int) (Slope * y + Intercept);
					coordsOnPath.add(new PointInGrid(x, y));
				}
				break;
			case 2: //runs while start x is less than end x.
					//right to left (both lower and upper side)
				//was ...; x <= (int)....
				for (int x = (int) pos.getX(); x >= (int) dest.getX(); x--) {
					int y = (int) ((x-Intercept)/Slope);
					coordsOnPath.add(new PointInGrid(x, y));
				}
				break;
			case 3: //runs while start y is greater than end y
					//top to bottom (both sides)
				for (int y = (int) pos.getY(); y >= (int) dest.getY(); y--) {
					int x = (int) (Slope * y + Intercept);
					coordsOnPath.add(new PointInGrid(x, y));
				}
				break;
			case 4: //runs while start x is less than end x
					//left to right (both lower and upper side)
				for (int x = (int) pos.getX(); x <= (int) dest.getX(); x++) {
					int y = (int) ((x-Intercept)/Slope);
					coordsOnPath.add(new PointInGrid(x, y));
				}
				break;
			default: //purpose? will it ever get triggered?
				coordsOnPath = pointsOnRoute(pos, dest);
		}		
		return coordsOnPath;
	}
	
	/***
	 * Evaluates all points in route.
	 * to see if there is an angle on the route towards the nearest ball from
	 * the side within the 85-95 degree angle.
	 * returns a null PointInGrid if there is an obstacle (barrier or cross) 
	 * 	between the robot and the ball
	 */
	@Override
	public PointInGrid CheckForPickupAngle(PointInGrid Robot, PointInGrid nextPoint, PointInGrid nearestBall) {
		
		PointInGrid Point = null;
		
		for (PointInGrid p : coordsOnPath) {
			double angle = Calculator.calc_Angle(nextPoint, p, nearestBall);
			
			if (angle >= 85 && angle <= 95) {
				Point = p;
				break;
			}
		}
		//from point to nearestball
		//check entire route for obstacles
		for (PointInGrid p : pointsOnRoute(Point, nearestBall))
		{
			if(ImageGrid[(int) p.getX()][(int) p.getY()] == OBSTACLE)
			{
				Point = null;
			}
		}
		return Point;
	}

	/**
	 * searches through all balls in list and reevalutes if one is closer than the other
	 * return: the point of the closest ball (nomatter its position)
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
	 * compiles a string for the robot to execute
	 * Robot: The middle of the robot
	 * conPoint: the front of the robot
	 */
	@Override
	public void Drive(PointInGrid conPoint, PointInGrid Robot, PointInGrid nextCornor, PointInGrid nearestBall) {		
		
		//this gets triggered if the EvalRoute could 
		//not find a point in which there was
		//an appropriate angle towards the ball without an obstacle in between
		if (CheckForPickupAngle(Robot, nextCornor, nearestBall) == null)
			//will compile the string to send to the robot so that the robot can drive
			//to the next corner
			Calculator.getDir(conPoint, Robot, nextCornor);
		
		//if there was a succesfull point with angle then
		//create the route 
		else
			Calculator.getDir(conPoint, Robot, CheckForPickupAngle(Robot, nextCornor, nearestBall));
			//Calculator.getDir(conPoint, Robot, EvalRoute(Robot, nextCornor, findNearestBall(Robot, BallPoints)));
		
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
		SimulatedGrid[5][4] = RobotMid;
		SimulatedGrid[5][5] = RobotFront;
			
		/**
		 * This inserts 6 balls into the system, whereas one is outside the main barrier.
		 */
		int ball = 4;
		SimulatedGrid[12][5] = ball;
		SimulatedGrid[10][10] = ball;
		SimulatedGrid[5][14] = ball;
		SimulatedGrid[3][6] = ball;
		SimulatedGrid[12][15] = ball;
		SimulatedGrid[19][19] = ball;
	}
	/**
	 * Finds elements in grid (used for simulation) 
	 * In the actual implementation, the image recog will send the grid info in lists
	 */
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
					listofBallCoords.add(tmpBall);
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
}