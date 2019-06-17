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

import org.junit.internal.runners.model.EachTestNotifier;

import RobotControl.Billedbehandling;
//import RobotControl.Billedbehandling_27032019;
import RobotControl.RemoteCarClient;
//import RouteCalculator.PointInGrid;

public class RouteLogic implements IRouteLogic, Runnable {

	//// SIMULATIONGRID AND SIMULATIONBALLS////
	private int[][] SimulatedGrid = new int[20][20];
	private List<Point> listofBallCoords = new ArrayList<Point>();

	// NOT SIMULATION VARIABLES//
	private int ballvalue = 4;
	private int checkpoint;
	private boolean visit1, visit2, visit3, visit4, onBallMission;
	private double upperWall, lowerWall, rightWall, leftWall;
	private List<Point> coordsOnPath;
	private Point robotMiddle, robotFront, xCenter;
	private Point ULcorner, LLcorner, URcorner, LRcorner;
	private Point safeHazardPoint, smallGoalSafeSpot, smallGoal, nextCornerToNavigateTo;
	private Point firstConnection, LastTouchedConnectionPoint, newConnectionPoint;
	private List<Point> ConnectionPoints, allBalls, cornerBalls, xBalls, xPickupPoints, cornerPickupPoints, dangerBalls,
			safeBalls, dangerPickupPoints, allPickUpPoints, corners;
	private List<Point> xPoints, newConnectionpoints;
	private int[][] ImageGrid;
	private final int OBSTACLE = 1, HAZARD = 20;
	boolean firstConnectionFound, firstConnectionTouched, programStillRunning, readyToNavigateToAHazardPoint,
			pickupBall;
	private boolean returnToPrevHazardPoint, unloadBalls, SPINWIN, NavigateToNextConnectionPoint;
	private RemoteCarClient RC;
	// private Billedbehandling_27032019 ImageRec;
	private Billedbehandling ImageRec;
	private RouteCalculator Calculator;
	Scanner keyb = new Scanner(System.in); // Hvad er det her???

	private boolean firstTime;

	public RouteLogic(Billedbehandling BB) {
		this.ImageRec = BB;
		this.Calculator = new RouteCalculator();
		this.RC = Main.RC;

	}

	/**
	 * Constructor for the class
	 * 
	 * @param robotMiddle      rotaionCenter on robot
	 * @param robotFront       Point right in front of robot
	 * @param Balls            Locations of the balls on the track
	 * @param ConnectionPoints Four connectionPoints posing for the robot's overall
	 *                         path
	 * @param ImageGrid        2D array that imitates the entire track
	 */

	/**
	 * Finds the closest connection point that has a direct path in relation to the
	 * robot middle
	 * 
	 * @param robotMiddle      rotationCenter on robot
	 * @param ConnectionPoints Four connectionPoints posing for the robot's overall
	 *                         path
	 */
	private Point findClosestConnectionPoint(Point robotMiddle, List<Point> ConnectionPoints) {
		double dist = 10000;
		Point closestPoint = null;

		List<Point> connPointWithDirectPath = new ArrayList<Point>();
		// checks if the points have a direct path
		for (Point connPoint : ConnectionPoints) {
			if (checkDirectPath(pointsOnRoute(robotMiddle, connPoint)) == true) {
				connPointWithDirectPath.add(connPoint);
			}
		}

		// finds the closest of those with direct path
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

	// an implementation that picks up safe balls first, then dangerous balls
	// using two different rule sets.
	public void runningTwo() {

		firstTime = true;
		onBallMission = true;
		visit1 = false;
		visit2 = false;
		visit3 = false;
		visit4 = false;

		System.out.println("MADE IT INTO RUNNINGTWO");
		ImageGrid = new int[1920][1080];

		dangerBalls = new ArrayList<Point>();
		dangerPickupPoints = new ArrayList<Point>();
		safeBalls = new ArrayList<Point>();
		allPickUpPoints = new ArrayList<Point>(); // Safeballs and dangerPickupPoints
		cornerBalls = new ArrayList<Point>();
		cornerPickupPoints = new ArrayList<Point>();
		xBalls = new ArrayList<Point>();
		xPickupPoints = new ArrayList<Point>();

//		this.ConnectionPoints = new ArrayList<Point>();
//		this.Balls = new ArrayList<Point>();

		this.programStillRunning = true;
		int counter = 0;
		Point nearestBall;
		while (this.programStillRunning) {
			// if (RC.GetSendingStatus() == false) { //if the sending status returned is
			// false

			while (RC.robotExecuting) {
				System.out.print("");
			}

			/*
			 * try { Thread.sleep(6000); } catch (InterruptedException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 */

			// Get info from imageRec.
			ImageRec.runImageRec();
			GetImageInfo();

			findAllPickupPoints();

			/*
			 * for (int i = 0; i < ImageRec.listOfBallCoordinates.size(); i++) {
			 * System.out.println("Ball coordinate : x = "
			 * +ImageRec.listOfBallCoordinates.get(i).x+ " y = "
			 * +ImageRec.listOfBallCoordinates.get(i).y); }
			 * System.out.println("Green robot marker : x = " +ImageRec.robotGreenMarker.x+
			 * " y = " +ImageRec.robotGreenMarker.y);
			 * System.out.println("Blue  robot marker : x = " +ImageRec.robotBlueMarker.x+
			 * " y = " +ImageRec.robotBlueMarker.y +
			 * "_________________________________________________________");
			 */

			
			
			List<Point> ballsWithDirectPathFromRobot = allPickUpPoints;
			/*BallsWithDirectPathFunc(robotMiddle, allPickUpPoints);*/
			// find all balls with a direct path
			
			if (SPINWIN) {
				// CommunicateToServer("0F:3;0R:1500;0S:300;0B:true");
				// this stops the while loop
				this.programStillRunning = false;
			} else if (!ballsWithDirectPathFromRobot.isEmpty()) {
				// finds the safest ball and communicates to the server
				// counter =
				NearestSafeBallPickupAlgorithm(ballsWithDirectPathFromRobot, counter);
				onBallMission = true;
			}
//			else if(safeBalls.isEmpty() && !dangerBalls.isEmpty())//no more safe balls and still dangerous balls
//			{
//				HazardBallPickupAlgorithm();
//			}
			else if (allBalls.isEmpty()) {
				// CommunicateToServer("0F:12;0R:0;0S:0;0B:true;");
				// SPINWIN = true;
				HeadForGoalAndUnload();
			}
//			else if(safeBalls.isEmpty() && dangerBalls.isEmpty()) //go to goal
//			{
//				//check for direct path to goal safe spot (not through hazard zone)
//				HeadForGoalAndUnload();
//			}
			// We are at a point and can't reach any safe balls
//			else if (checkIfCoordsNear(robotMiddle, newConnectionPoint, 15)) { // sets the new connectionPoint
//				// all this gets triggered if the robot has reached the newConnection point
//				newConnectionPoint = nextConnPoint(robotMiddle, ConnectionPoints);
//			} 
			else { // we are not at a point and there is safe balls but they
						// can't get picked up
						// finds closest connectionPoint and drives to it.

				if (!onBallMission) {
					int connectionpointNumer = newConnectionpoints.indexOf(newConnectionPoint);
					
					switch (connectionpointNumer) {
					case 0:
						newConnectionPoint = newConnectionpoints.get(1);
						break;
					case 1:
						newConnectionPoint = newConnectionpoints.get(2);
						break;
					case 2:
						newConnectionPoint = newConnectionpoints.get(3);
						break;
					case 3:
						newConnectionPoint = newConnectionpoints.get(0);
						break;
					}

				} else {

					newConnectionPoint = findClosestConnectionPoint(robotMiddle, newConnectionpoints);
				}
				
				
				
				while (!checkIfCoordsNear(robotFront, newConnectionPoint, 20)) // old 12
				{

					String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
				
					CommunicateToServer(commandToSend);

					while (RC.robotExecuting) {
						System.out.print("");
					}

					ImageRec.runImageRec();

					GetImageInfo();
				}
				
				onBallMission = false;
			}
		}
	}

	private void findAllPickupPoints() {
		// FIND ALL PICKUPPOINTS

		int wallMargin = 40;
		int pickupDist = 90;
		int wallCorrectionDist = 5;
		int cornerPickupDist = 100;
		int cornerCorrectionDist = 0;

		if (!dangerBalls.isEmpty()) {
			dangerBalls.clear();
		}
		if (!dangerPickupPoints.isEmpty()) {
			dangerPickupPoints.clear();
		}
		if (!safeBalls.isEmpty()) {
			safeBalls.clear();
		}
		if (!allPickUpPoints.isEmpty()) {
			allPickUpPoints.clear();
		}
		if (!cornerPickupPoints.isEmpty()) {
			cornerPickupPoints.clear();

		}
		if (!cornerBalls.isEmpty()) {
			cornerBalls.clear();
		}

		for (Point point : allBalls) {
			// Close to upper wall
			if (point.getY() < upperWall + wallMargin) {

				// ULcorner
				if (point.getX() < leftWall + wallMargin) {
					cornerPickupPoints.add(
							new Point((int) point.getX() + cornerPickupDist, (int) point.getY() + cornerPickupDist));
					cornerBalls.add(new Point((int) point.getX() + cornerCorrectionDist,
							(int) point.getY() + cornerCorrectionDist));
				}
				// URcorner
				else if (point.getX() > rightWall - wallMargin) {
					cornerPickupPoints.add(
							new Point((int) point.getX() - cornerPickupDist, (int) point.getY() + cornerPickupDist));
					cornerBalls.add(new Point((int) point.getX() - cornerCorrectionDist,
							(int) point.getY() + cornerCorrectionDist));
				} else {
					dangerBalls.add(new Point((int) point.getX(), (int) point.getY() + wallCorrectionDist));
					dangerPickupPoints.add(new Point((int) point.getX(), (int) point.getY() + pickupDist));
				}
			}
			// Close to lower wall
			else if (point.getY() > lowerWall - wallMargin) {

				// LLcorner
				if (point.getX() < leftWall + wallMargin) {
					cornerPickupPoints.add(
							new Point((int) point.getX() + cornerPickupDist, (int) point.getY() - cornerPickupDist));
					cornerBalls.add(new Point((int) point.getX() + cornerCorrectionDist,
							(int) point.getY() - cornerCorrectionDist));
				}
				// LRcorner
				else if (point.getX() > rightWall - wallMargin) {
					cornerPickupPoints.add(
							new Point((int) point.getX() - cornerPickupDist, (int) point.getY() - cornerPickupDist));
					cornerBalls.add(new Point((int) point.getX() - cornerCorrectionDist,
							(int) point.getY() - cornerCorrectionDist));
				} else {
					dangerBalls.add(new Point((int) point.getX(), (int) point.getY() - wallCorrectionDist));
					dangerPickupPoints.add(new Point((int) point.getX(), (int) point.getY() - pickupDist));
				}
			}
			// Close to leftside wall
			else if (point.getX() < leftWall + wallMargin) {
				dangerBalls.add(new Point((int) point.getX() + wallCorrectionDist, (int) point.getY()));
				dangerPickupPoints.add(new Point((int) point.getX() + pickupDist, (int) point.getY()));
			}
			// Close to rightside wall
			else if (point.getX() > rightWall - wallMargin) {
				dangerBalls.add(new Point((int) point.getX() - wallCorrectionDist, (int) point.getY()));
				dangerPickupPoints.add(new Point((int) point.getX() - pickupDist, (int) point.getY()));
			}
			// tilf�j flere else if til krydset i midten
			else {
				safeBalls.add(point);
			}

		}

		allPickUpPoints.addAll(safeBalls);
		allPickUpPoints.addAll(dangerPickupPoints);
		allPickUpPoints.addAll(cornerPickupPoints);

		System.out.println("Upperwall: " + upperWall);
		System.out.println("Lowerwall: " + lowerWall);
		System.out.println("Rightwall: " + rightWall);
		System.out.println("Leftwall: " + leftWall);

		System.out.println("ALLBALLS");
		for (Point point : allBalls)
			System.out.println(point.getX() + ", " + point.getY());

		System.out.println("SAFEBALLS");
		for (Point point : safeBalls)
			System.out.println(point.getX() + ", " + point.getY());
		System.out.println("DANGERBALLS");
		for (Point point : dangerBalls)
			System.out.println(point.getX() + ", " + point.getY());
		System.out.println("DANGERBALLS PICKUPPOINT");
		for (Point point : dangerPickupPoints)
			System.out.println(point.getX() + ", " + point.getY());

		System.out.println("CORNERBALLS");
		for (Point point : cornerBalls)
			System.out.println(point.getX() + ", " + point.getY());
		System.out.println("CORNERBALLS PICKUPPOINT");
		for (Point point : cornerPickupPoints)
			System.out.println(point.getX() + ", " + point.getY());
		// keyb.next();
	}

	// GETS INFO FROM imagerecognition
	public void GetImageInfo() {
		robotMiddle = ConvertPoint(ImageRec.robotGreenMarker);
		robotFront = ConvertPoint(ImageRec.robotBlueMarker);
		allBalls = ConvertPoint(ImageRec.listOfBallCoordinates);

		List<Point> corners = ConvertPoint(ImageRec.getCorners());
//		ULcorner = new Point(407,166);
//		LLcorner = new Point(410,896);
//		URcorner = new Point(1401,171);
//		LRcorner = new Point(1400,888);

		if (firstTime) {

			int connectionDist = 150;
			
//			xCenter = ConvertPoint(ImageRec.getCrossCenterPoint());
//			
//			for(Point point: markX(xCenter.getX(),xCenter.getY(),200)) {
//				ImageGrid[(int)point.getX()-1][(int)point.getY()-1] = 1;
//			}
//			

			newConnectionpoints = new ArrayList<Point>(4);

			ULcorner = corners.get(0);
			LLcorner = corners.get(1);
			URcorner = corners.get(2);
			LRcorner = corners.get(3);

			if (ULcorner.getX() > LLcorner.getX()) {
				leftWall = ULcorner.getX();
			} else {
				leftWall = LLcorner.getX();
			}

			if (URcorner.getX() > LRcorner.getX()) {
				rightWall = LRcorner.getX();
			} else {
				rightWall = URcorner.getX();
			}

			if (ULcorner.getY() > URcorner.getY()) {
				upperWall = ULcorner.getY();
			} else {
				upperWall = URcorner.getY();
			}

			if (LLcorner.getY() > LRcorner.getY()) {
				lowerWall = LRcorner.getY();
			} else {
				lowerWall = LLcorner.getY();
			}

			newConnectionpoints
					.add(new Point((int) URcorner.getX() + connectionDist, (int) URcorner.getY() + connectionDist));
			newConnectionpoints
					.add(new Point((int) LRcorner.getX() + connectionDist, (int) LRcorner.getY() - connectionDist));
			newConnectionpoints
					.add(new Point((int) ULcorner.getX() - connectionDist, (int) ULcorner.getY() + connectionDist));
			newConnectionpoints
					.add(new Point((int) LLcorner.getX() - connectionDist, (int) LLcorner.getY() - connectionDist));

			firstTime = false;
		}

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
	 * 
	 * @param pos  : robotMiddle
	 * @param dest : Destination point
	 * @return : List of Point: Containing all points between two points
	 */
	public List<Point> pointsOnRoute(Point robotMiddle, Point dest) {

		coordsOnPath = new ArrayList<Point>();

		double Slope = (dest.getX() - robotMiddle.getX()) / (dest.getY() - robotMiddle.getY());
		double Intercept = robotMiddle.getX() - Slope * robotMiddle.getY();

		// Needs fixing
		if (robotMiddle.getX() <= dest.getX()
				&& Math.abs(robotMiddle.getY() - dest.getY()) < Math.abs(robotMiddle.getX() - dest.getX()))
			checkpoint = 4;
		if (robotMiddle.getY() >= dest.getY()
				&& Math.abs(robotMiddle.getY() - dest.getY()) >= Math.abs(robotMiddle.getX() - dest.getX()))
			checkpoint = 3;
		if (robotMiddle.getX() >= dest.getX()
				&& Math.abs(robotMiddle.getY() - dest.getY()) < Math.abs(robotMiddle.getX() - dest.getX()))
			checkpoint = 2;
		if (robotMiddle.getY() <= dest.getY()
				&& Math.abs(robotMiddle.getY() - dest.getY()) >= Math.abs(robotMiddle.getX() - dest.getX()))
			checkpoint = 1;

		// TODO SOMEHOW TRIGGER checkpoint case 1,2,3 and 4? What if the robot is not on
		// one of those?
		// ikke initialiseret nogle steder, s� altid ende i default?
		// case 1 and 2 is deliberately < and > as case 3 and 4 handles <= and >=.
		switch (checkpoint) {
		case 1: // runs while start y is less than end y.
				// bottom to top (both sides)
			for (int y = (int) robotMiddle.getY(); y < (int) dest.getY(); y++) {
				int x = (int) (Slope * y + Intercept);
				coordsOnPath.add(new Point(x, y));
			}
			break;
		case 2: // runs while start x is less than end x.
				// right to left (both lower and upper side)
			// was ...; x <= (int)....
			for (int x = (int) robotMiddle.getX(); x > (int) dest.getX(); x--) {
				int y = (int) ((x - Intercept) / Slope);
				coordsOnPath.add(new Point(x, y));
			}
			break;
		case 3: // runs while start y is greater than end y
				// top to bottom (both sides)
			for (int y = (int) robotMiddle.getY(); y >= (int) dest.getY(); y--) {
				int x = (int) (Slope * y + Intercept);
				coordsOnPath.add(new Point(x, y));
			}
			break;
		case 4: // runs while start x is less than end x
				// left to right (both lower and upper side)
			for (int x = (int) robotMiddle.getX(); x <= (int) dest.getX(); x++) {
				int y = (int) ((x - Intercept) / Slope);
				coordsOnPath.add(new Point(x, y));
			}
			break;
		default: // purpose? will it ever get triggered?
			coordsOnPath = pointsOnRoute(robotMiddle, dest);
			break;
		}

		return coordsOnPath;
	}

	/***
	 * Evaluates all points in route. to see if there is an angle on the route
	 * towards the nearest ball from the side within the 85-95 degree angle. returns
	 * a null Point if there is an obstacle (barrier or cross) between the robot and
	 * the ball
	 * 
	 * @param Robot        : RobotMiddle
	 * @param nextPoint    : a point on path, should be null atm
	 * @param nearestBall  : the closest ball
	 * @param pointsOnPath : The path between two points in coords
	 */
	// TODO FIX IMPLEMENTATION (remove nextPoint and use the correct list in actual
	// impl)
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
	 * searches through all balls in list and reevalutes if one is closer than the
	 * other
	 * 
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
	 * 
	 * @param Robot:      The middle of the robot
	 * @param conPoint:   the front of the robot
	 * @param nextCornor  : ??
	 * @param nearestBall : a ball to consider in the method
	 */
	// TODO CHANGE IMPLEMENTATION
	@Override
	public void Drive(Point conPoint, Point Robot, Point nextCornor, Point nearestBall) {

		// this gets triggered if the EvalRoute could
		// not find a point in which there was
		// an appropriate angle towards the ball without an obstacle in between
		if (CheckPickupAngleOnRoute(Robot, nextCornor, nearestBall, null) == null) {
			// will compile the string to send to the robot so that the robot can drive
			// to the next corner
			String commandToSend = Calculator.getDir(conPoint, Robot, nextCornor);
			CommunicateToServer(commandToSend);
		}
		// if there was a succesfull point with angle then
		// create the route
		else {
			String commandToSend = Calculator.getDir(conPoint, Robot,
					CheckPickupAngleOnRoute(Robot, nextCornor, nearestBall, null));
			CommunicateToServer(commandToSend);
			// Calculator.getDir(conPoint, Robot, EvalRoute(Robot, nextCornor,
			// findNearestBall(Robot, BallPoints)));
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
	 * Checks all points to see if an obstacle occurs on the route.
	 * 
	 * @param directpath : List of coords on direct path computed by pointsOnRoute()
	 * @return : true if no obstacles or false if obstacles
	 */
	public boolean checkDirectPath(List<Point> directpath) {
		for (Point p : directpath) {
			if (ImageGrid[(int) p.getX()][(int) p.getY()] == OBSTACLE) {
				return false;
			}
		}
		return true;
	}

	// x is robotmiddle.getX(), y is robotmiddle.getY().
	// r is from dist form between robotMiddle and robotFront.
	/**
	 * finds the robots circumference were it to do a 360 rotation
	 * 
	 * @param RmidX       : the robot middle x coord
	 * @param RmidY       : the robot middle y coord
	 * @param robotRadius : the distance between robot middle and robot front
	 * @return
	 */
	public static List<Point> markX(double RmidX, double RmidY, double robotRadius) {
		List<Point> CirclePoints = new ArrayList<Point>();

		double PI = 3.1415926535;
		double i, angle, x1, y1;

		// iterates through all 360 angles
		for (i = 0; i < 360; i += 1) {
			angle = i;
			// finds coordinates on sin and cos with radius
			x1 = robotRadius * Math.cos(angle * PI / 180);
			y1 = robotRadius * Math.sin(angle * PI / 180);
			//
			int ElX = (int) (RmidX + x1);
			int ElY = (int) (RmidY + y1);
			// SimulatedGrid[ElX][ElY] = 1;
			// setElementColor(color);
			CirclePoints.add(new Point(ElX, ElY));
		}
		return CirclePoints;
	}

	/**
	 * Finds balls with direct path (no obstacles inbetween)
	 * 
	 * @param robotMiddle
	 * @param balls
	 * @return a list of Point
	 */
	public List<Point> BallsWithDirectPath(Point robotMiddle, List<Point> balls) {
		List<Point> ballsWithDirectPath = new ArrayList<Point>();
		// for each ball, check its path and put it into a list if no obstacles
		for (Point ballPoint : balls) {
			if (checkDirectPath(pointsOnRoute(robotMiddle, ballPoint))) // gets route, checks route
			{
				for (Point p : coordsOnPath)
					System.out.println("GETX: " + p.getX() + ", GETY: " + p.getY() + ", ALL: "
							+ SimulatedGrid[(int) p.getX()][(int) p.getY()]);
				System.out.println("BOOL: " + checkDirectPath(pointsOnRoute(robotMiddle, ballPoint)));
				keyb.next();
				ballsWithDirectPath.add(ballPoint);
			}
		}
		return ballsWithDirectPath;
	}

	// sends a string to the server
	public void CommunicateToServer(String command) {
		RC.SendCommandString(command);
	}

	// checks if two Points coords are equals
	public boolean checkIfCoordsEqual(Point robotMiddle, Point dest) {
		if (robotMiddle.getX() == dest.getX() && robotMiddle.getY() == dest.getY())
			return true;

		return false;
	}

	// checks if two Points coords are equals
	public boolean checkIfCoordsNear(Point robotMiddle, Point dest, double d) {
		System.out.println("Dif. on x-axis" + (robotMiddle.getX() - dest.getX()));
		System.out.println("Dif. on y-axis" + (robotMiddle.getY() - dest.getY()));

//		if(robotMiddle.getX() < dest.getX()+error_margin && robotMiddle.getX() > dest.getX()-error_margin &&
//		 robotMiddle.getY() < dest.getY()+error_margin && robotMiddle.getY() > dest.getY()-error_margin
//				) return true;

		if (robotMiddle.getX() < dest.getX() + d && robotMiddle.getX() > dest.getX() - d
				&& robotMiddle.getY() < dest.getY() + d && robotMiddle.getY() > dest.getY() - d)
			return true;
		return false;
	}

	/**
	 * finds the next connectionPoint as long as the robot is on one
	 */
	public Point nextConnPoint(Point robotMiddle, List<Point> ConnectionPoints) {
		int connPointIndexCount = 0;
		for (Point point : ConnectionPoints) {

			if (robotMiddle.getX() == point.getX() && robotMiddle.getY() == point.getY()) {
				if (connPointIndexCount == 0)
					return ConnectionPoints.get(2); // from top left to bottom left
				if (connPointIndexCount == 1)
					return ConnectionPoints.get(0); // from top right to top left
				if (connPointIndexCount == 2)
					return ConnectionPoints.get(3); // from bottom left to bottom right
				if (connPointIndexCount == 3)
					return ConnectionPoints.get(1); // from bottom right to top right
			}
		}
		return null;
	}

	/**
	 * drives to closest connectionPoint checks if it can find a hazard point it can
	 * navigate to navigates to it, navigates to closest ball thereafter returns to
	 * hazardpoint returns to closest connectionPoint
	 */
	public void HazardBallPickupAlgorithm() {
		if (readyToNavigateToAHazardPoint == true) // we are at a connPoint ready to get some dangerballs
		{ // finds safe pickup point - navigate to it if possible
			safeHazardPoint = findNearestBall(robotMiddle, dangerPickupPoints);
			// not using directpathHazardCheck because the hazard spot can be in the hazard
			// zone.
			boolean allowTrip = checkDirectPath(pointsOnRoute(robotMiddle, safeHazardPoint));
			if (allowTrip == true) // will be adjusted automatically next time method is called
			{
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
				CommunicateToServer(commandToSend);
				if (checkIfCoordsEqual(robotMiddle, safeHazardPoint)) {
					readyToNavigateToAHazardPoint = false;
					pickupBall = true;
				}
			} else { // find next connectionPoint on route
				nextCornerToNavigateTo = nextConnPoint(robotMiddle, ConnectionPoints);
				readyToNavigateToAHazardPoint = false;
				NavigateToNextConnectionPoint = true;
			}
		} else if (pickupBall == true) // if we have reached a hazard point and we can pickup the ball
		{
			newConnectionPoint = findNearestBall(robotMiddle, dangerBalls);
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
			CommunicateToServer(commandToSend);
			if (checkIfCoordsEqual(robotMiddle, newConnectionPoint)) {
				pickupBall = false;
				returnToPrevHazardPoint = true;
			}
			CommunicateToServerPickup();
		} else if (returnToPrevHazardPoint == true) { // if we have returned to our safe hazard point
			newConnectionPoint = findClosestConnectionPoint(robotMiddle, ConnectionPoints);
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, safeHazardPoint);
			CommunicateToServer(commandToSend);
			if (checkIfCoordsEqual(robotMiddle, newConnectionPoint))
				returnToPrevHazardPoint = false;
		} else if (NavigateToNextConnectionPoint == true) {
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, nextCornerToNavigateTo);
			CommunicateToServer(commandToSend);
			if (checkIfCoordsEqual(robotMiddle, nextCornerToNavigateTo) == true) {
				readyToNavigateToAHazardPoint = true;
				NavigateToNextConnectionPoint = false;
			}
		} else { // constantly attempts to navigate to the closest connPoint
					// gets conn point and calculates string to robot
			newConnectionPoint = findClosestConnectionPoint(robotMiddle, ConnectionPoints);
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionPoint);
			CommunicateToServer(commandToSend);
			if (checkIfCoordsEqual(robotMiddle, newConnectionPoint)) {
				readyToNavigateToAHazardPoint = true;
			}
		}
	}

	/**
	 * sends the pickup command to the robot
	 */
	public void CommunicateToServerPickup() {
		CommunicateToServer("0F:11;0R:0;0S:0;0B:true;");
	}

	// picks up the nearest ball
	public Double NearestSafeBallPickupAlgorithm(List<Point> Balls, double counter) {
		// if the list isn't empty
		Point nearestBall = findNearestBall(robotFront, Balls);
		// Point nearestBall = findNearestBall(robotMiddle, safeBalls);
		while (!checkIfCoordsNear(robotFront, nearestBall, 10)) // old 12
		{
			/*
			 * if (checkIfCoordsNear(robotMiddle, nearestBall,
			 * Calculator.calc_Dist(robotMiddle, robotFront)-15.3)) {
			 * ImageRec.runImageRec(); GetImageInfo();
			 * CommunicateToServer("0F:2;0S:250;0R:300;0B:false"); nearestBall =
			 * findNearestBall(robotFront,safeBalls); }
			 */
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, nearestBall);
			// keyb.next();
			CommunicateToServer(commandToSend);

//			try {
//				Thread.sleep(6000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			while (RC.robotExecuting) {
				System.out.print("");
			}

			ImageRec.runImageRec();

			GetImageInfo();
		}

		if (safeBalls.contains(nearestBall)) {
			CommunicateToServerPickup();
			while (RC.robotExecuting) {
				System.out.print("");
			}
		} else if (dangerPickupPoints.contains(nearestBall)) {
			nearestBall = dangerBalls.get(dangerPickupPoints.indexOf(nearestBall));
			while (!checkIfCoordsNear(robotFront, nearestBall, 4)) {
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, nearestBall);
				CommunicateToServer(commandToSend);
				while (RC.robotExecuting) {
					System.out.print("");
				}

				ImageRec.runImageRec();
				GetImageInfo();
			}
			CommunicateToServerPickup();
			while (RC.robotExecuting) {
				System.out.print("");
			}
			CommunicateToServer("0F:2;0R:720;0S:200;0B:true;");
			while (RC.robotExecuting) {
				System.out.print("");
			}

		} else if (cornerPickupPoints.contains(nearestBall)) {
			nearestBall = cornerBalls.get(cornerPickupPoints.indexOf(nearestBall));
			while (!checkIfCoordsNear(robotFront, nearestBall, 40)) {
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, nearestBall);
				CommunicateToServer(commandToSend);
				while (RC.robotExecuting) {
					System.out.print("");
				}

				ImageRec.runImageRec();
				GetImageInfo();
			}
			CommunicateToServerPickup();
			while (RC.robotExecuting) {
				System.out.print("");
			}
			CommunicateToServer("0F:2;0R:720;0S:200;0B:true;");
			while (RC.robotExecuting) {
				System.out.print("");
			}
		} else if (xPickupPoints.contains(nearestBall)) {
			nearestBall = xBalls.get(xPickupPoints.indexOf(nearestBall));
			while (!checkIfCoordsNear(robotFront, nearestBall, 40)) {
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, nearestBall);
				CommunicateToServer(commandToSend);
				while (RC.robotExecuting) {
					System.out.print("");
				}

				ImageRec.runImageRec();
				GetImageInfo();
			}
			xPickup();
			while (RC.robotExecuting) {
				System.out.print("");
			}
		}

		System.out.println("Picked it up!!");

		// Scenario to get close ball
		/*
		 * if(counter == 0) { robotFront = new Point(4,5); findElementsInGrid();
		 * counter++; } else if(counter == 1) { robotFront = new Point(3,6); robotMiddle
		 * = new Point(4,5); safeBalls.remove(safeBalls.lastIndexOf(nearestBall));
		 * findElementsInGrid(); CommunicateToServerPickup(); }
		 */
		return counter; // the return is used for testing
	}

	// heads for goal safe spot and unloads
	public void HeadForGoalAndUnload() {
		// OUR OLD VERSION
		/*
		 * Point goalPointOne = new
		 * Point(100,(int)LLcorner.getY()-(((int)LLcorner.getY()-(int)ULcorner.getY())/2
		 * )); Point goalPointTwo = new
		 * Point(50,(int)LLcorner.getY()-(((int)LLcorner.getY()-(int)ULcorner.getY())/2)
		 * );
		 * 
		 * CommunicateToServer("0F:12;0R:0;0S:0;0B:true;");
		 */

		boolean running = true;
		boolean pointOneReached = false;
		boolean pointTwoReached = false;

		int middleX = (int) ULcorner.getX();
		int middleY = (int) LLcorner.getY() - (((int) LLcorner.getY() - (int) ULcorner.getY()) / 2);

		Point goalMiddle = new Point(middleX, middleY);
		Point goalPointOne = new Point((int) goalMiddle.getX() + 150, middleY);
		Point goalPointTwo = new Point((int) goalMiddle.getX() + 25, middleY);

		// initialiing points (should not be a problem)

		// task 1 is to get to goalspot 1.
		// we want to keep going until we have reached that point
		while (running) {
			// loop until we reached point one
			while (!checkIfCoordsNear(robotFront, goalPointOne, 20)) {
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, goalPointOne);
				CommunicateToServer(commandToSend);
				System.out.println("Going For One ----------------------------------------");
				while (RC.robotExecuting) {
					System.out.print("");
				}
				/*
				 * if(checkIfCoordsNear(robotFront, goalPointOne, 30)) {
				 * CommunicateToServer("0F:2;0S:250;0R:1000;0B:false");
				 * 
				 * ImageRec.runImageRec(); GetImageInfo();
				 * 
				 * while(RC.robotExecuting) { System.out.print(""); } }
				 */
				ImageRec.runImageRec();
				GetImageInfo();
			}
			// loop until we reached point two
			while (!checkIfCoordsNear(robotFront, goalPointTwo, 20)) {
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, goalPointTwo);
				CommunicateToServer(commandToSend);
				System.out.println("Going For Two ----------------------------------------");
				while (RC.robotExecuting) {
					System.out.print("");
				}
				ImageRec.runImageRec();
				GetImageInfo();

				// has larger margin, so the robot should start unloading
				if (checkIfCoordsNear(robotFront, goalPointTwo, 25)) {
					String command = Calculator.turn(robotFront, robotMiddle, goalMiddle);
					CommunicateToServer(command);

					while (RC.robotExecuting) {
						System.out.print("");
					}

					SPINWIN = true;
					CommunicateToServer("0F:13;0R:600;0S:100;0B:true"); // K?r armen helt op!
					CommunicateToServer("0F:12;0R:0;0S:0;0B:true;"); // Smid bolde ud
				}
			}
		}
	}

	public boolean checkForObstacles(Point robotMiddle, Point dest, Point xMiddle, int radius) {
		double Slope = (dest.getY() - robotMiddle.getY()) / (dest.getX() - robotMiddle.getX());
		double Intercept = robotMiddle.getY() - Slope * robotMiddle.getX();
		
		double dist = Math.abs(Slope * xMiddle.getX() - Intercept - xMiddle.getY())/Math.sqrt(Math.pow(Slope, 2) + 1);
		
		if (radius >= dist) return true;
		
		else return false;
	}
	
	// checks if a direct path touches hazard zones or obstacles
	public boolean checkDirectPathObstacleHazard(List<Point> directpath) {
		for (Point p : directpath) {
			if (ImageGrid != null) {
				if (ImageGrid[(int) p.getX() - 1][(int) p.getY() - 1] == OBSTACLE
						|| ImageGrid[(int) p.getX() - 1][(int) p.getY() - 1] == HAZARD) {
					return false;
				}
			}
			// checks if the simulatedGrid is initialized or if the actual grid is.
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

	// returns a list of balls that isn't obstructed by obstacles or a hazard zone
	public List<Point> BallsWithDirectPathFunc(Point robotMiddle, List<Point> balls) {
		List<Point> ballsWithDirectPath = new ArrayList<Point>();
		// for each ball, check its path and put it into a list if no obstacles
		for (Point ballPoint : balls) {
			if (checkDirectPathObstacleHazard(pointsOnRoute(robotMiddle, ballPoint))) // gets route, checks route
			{
				ballsWithDirectPath.add(ballPoint);
			}
		}
		return ballsWithDirectPath;
	}

	public Point ConvertPoint(org.opencv.core.Point point) {
		Point p = new Point((int) point.x, (int) point.y);
		return p;
	}

	public List<Point> ConvertPoint(List<org.opencv.core.Point> pointList) {
		List<Point> p = new ArrayList<Point>();
		for (org.opencv.core.Point op : pointList) {
			p.add(ConvertPoint(op));
		}
		return p;
	}

	public void xPickup() {
		// K�r arm lidt ned
		CommunicateToServer("OF:14;OR:0;OS:0;OB:true");
		while (RC.robotExecuting) {
			System.out.print("");
		}
		// Bak lidt
		CommunicateToServer("OF:2;OR:70;OS:100;OB:true");
		while (RC.robotExecuting) {
			System.out.print("");
		}
		// Arm helt ned og op
		CommunicateToServer("OF:15;OR:0;OS:0;OB:true");
		while (RC.robotExecuting) {
			System.out.print("");
		}
		// Bak lidt
		CommunicateToServer("OF:2;OR:130;OS:100;OB:true");
		while (RC.robotExecuting) {
			System.out.print("");
		}

	}

}
