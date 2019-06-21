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

public class RouteLogic implements IRouteLogic{

	// NOT SIMULATION VARIABLES//
	Point goalMiddle;
	Point goalPointOne;
	Point goalPointTwo;
	
	
	private int ballsOnMap;
	private boolean onBallMission, firstUnloadDone;
	private double upperWall, lowerWall, rightWall, leftWall;
	private Point robotMiddle, robotFront, xCenter;
	private Point ULcorner, LLcorner, URcorner, LRcorner;
	private Point newConnectionPoint;
	private List<Point> allBalls, cornerBalls, xBalls, xPickupPoints, cornerPickupPoints, dangerBalls,
			safeBalls, dangerPickupPoints, allPickUpPoints;
	private List<Point> newConnectionpoints;
	private final int RADIUS = 125;
	boolean firstConnectionFound, firstConnectionTouched, programStillRunning, readyToNavigateToAHazardPoint,
			pickupBall;
	private RemoteCarClient RC;
	// private Billedbehandling_27032019 ImageRec;
	private Billedbehandling ImageRec;
	private RouteCalculator Calculator;
	Scanner keyb = new Scanner(System.in); // Hvad er det her???

	private boolean firstTime, shotOnGoal;

	public RouteLogic(Billedbehandling BB) {
		this.ImageRec = BB;
		this.Calculator = new RouteCalculator();
		this.RC = Main.RC;

	}

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
			if (isPathClear(robotMiddle, connPoint, xCenter)) {
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
		
		firstUnloadDone = false;
		shotOnGoal = false;
		firstTime = true;
		onBallMission = true;
		
		ballsOnMap = 0;

		System.out.println("MADE IT INTO RUNNINGTWO");
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
		
		System.out.println("Ready to start!!");
//		keyb.next();
		long startTime = System.nanoTime();
		
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

			
			List<Point> ballsWithDirectPathFromRobot = BallsWithDirectPathFunc(robotMiddle, allPickUpPoints);
			
			if(robotMiddle.getX() > 1000) {
				boolean rightBall = false;
				List<Point> rightBalls = new ArrayList<Point>();
				
				for (Point point : ballsWithDirectPathFromRobot) {
					if(point.getX() > 1200) {
						rightBall = true;
						rightBalls.add(point);
					}
				}
				
				if(rightBall) {
					ballsWithDirectPathFromRobot.clear();
					ballsWithDirectPathFromRobot.addAll(rightBalls);
				}
			}
			
			
			// find all balls with a direct path
			if (!ballsWithDirectPathFromRobot.isEmpty()) {
				// finds the safest ball and communicates to the server
				NearestSafeBallPickupAlgorithm(ballsWithDirectPathFromRobot, counter);
				onBallMission = true;
				shotOnGoal = false;
			}
			else if(allBalls.isEmpty() && shotOnGoal) {
				this.programStillRunning = false;
				long totalTime = System.nanoTime() - startTime;			
				double seconds = (double)totalTime / 1000000000.0;
				System.out.println("OVERALL TIME : " + seconds + " seconds");
			}
			else if (allBalls.isEmpty() && isPathClear(robotMiddle, goalPointOne, xCenter)) {
				onBallMission = true;
				HeadForGoalAndUnload();
			}
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
						newConnectionPoint = newConnectionpoints.get(3);
						break;
					case 2:
						newConnectionPoint = newConnectionpoints.get(0);
						break;
					case 3:
						newConnectionPoint = newConnectionpoints.get(2);
						break;
					}

				} else {

					newConnectionPoint = findClosestConnectionPoint(robotMiddle, newConnectionpoints);
				}
				
				
				
				while (!checkIfCoordsNear(robotFront, newConnectionPoint, 30)) // old 12
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
		int wallCorrectionDist = 8;
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
		
		if (!xPickupPoints.isEmpty()) {
			xPickupPoints.clear();

		}
		if (!xBalls.isEmpty()) {
			xBalls.clear();
		}
		
		xBalls = ConvertPoint(ImageRec.getCrossBalls());
		xPickupPoints = ConvertPoint(ImageRec.getCrossBallsPickup());

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
				dangerBalls.add(new Point((int) point.getX()  - wallCorrectionDist, (int) point.getY()));
				dangerPickupPoints.add(new Point((int) point.getX() - pickupDist, (int) point.getY()));
			}
			// tilfï¿½j flere else if til krydset i midten
			else if(xBalls.contains(point)) {
				
			}
			else {
				safeBalls.add(point);
			}

		}

		allPickUpPoints.addAll(safeBalls);
		allPickUpPoints.addAll(dangerPickupPoints);
		allPickUpPoints.addAll(cornerPickupPoints);
		allPickUpPoints.addAll(xPickupPoints);
//
//		System.out.println("Upperwall: " + upperWall);
//		System.out.println("Lowerwall: " + lowerWall);
//		System.out.println("Rightwall: " + rightWall);
//		System.out.println("Leftwall: " + leftWall);
//
//		System.out.println("ALLBALLS");
//		for (Point point : allBalls)
//			System.out.println(point.getX() + ", " + point.getY());
//
//		System.out.println("SAFEBALLS");
//		for (Point point : safeBalls)
//			System.out.println(point.getX() + ", " + point.getY());
//		System.out.println("DANGERBALLS");
//		for (Point point : dangerBalls)
//			System.out.println(point.getX() + ", " + point.getY());
//		System.out.println("DANGERBALLS PICKUPPOINT");
//		for (Point point : dangerPickupPoints)
//			System.out.println(point.getX() + ", " + point.getY());
//
//		System.out.println("CORNERBALLS");
//		for (Point point : cornerBalls)
//			System.out.println(point.getX() + ", " + point.getY());
//		System.out.println("CORNERBALLS PICKUPPOINT");
//		for (Point point : cornerPickupPoints)
//			System.out.println(point.getX() + ", " + point.getY());
		// keyb.next();
		
		int middleX = (int) ULcorner.getX();
		int middleY = (int) LLcorner.getY() - (((int) LLcorner.getY() - (int) ULcorner.getY()) / 2);

		goalMiddle = new Point(middleX, middleY);
		goalPointOne = new Point((int) goalMiddle.getX() + 150, middleY);
		goalPointTwo = new Point((int) goalMiddle.getX() + 50, middleY);
		
		
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

			int connectionDist = 75;
			
			xCenter = ConvertPoint(ImageRec.getCrossCenterPoint());
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
					.add(new Point((int)URcorner.getX() - (int)(Math.abs((URcorner.getX()-xCenter.getX()))*0.75), (int)URcorner.getY() + (int)(Math.abs((URcorner.getY()-xCenter.getY()))*0.5)));
			newConnectionpoints
					.add(new Point((int)LRcorner.getX() - (int)(Math.abs((LRcorner.getX()-xCenter.getX()))*0.75), (int)LRcorner.getY() - (int)(Math.abs((LRcorner.getY()-xCenter.getY()))*0.5)));
			newConnectionpoints
					.add(new Point((int)ULcorner.getX() + (int)(Math.abs((ULcorner.getX()-xCenter.getX()))*0.75), (int)ULcorner.getY() + (int)(Math.abs((ULcorner.getY()-xCenter.getY()))*0.5)));
			newConnectionpoints
					.add(new Point((int)LLcorner.getX() + (int)(Math.abs((LLcorner.getX()-xCenter.getX()))*0.75), (int)LLcorner.getY() - (int)(Math.abs((LLcorner.getY()-xCenter.getY()))*0.5)));

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


	// sends a string to the server
	public void CommunicateToServer(String command) {
		RC.SendCommandString(command);
	}

	// checks if two Points coords are equals
	public boolean checkIfCoordsNear(Point robotMiddle, Point dest, double d) {
//		System.out.println("Robot middle: "+robotMiddle.getX()+" , "+robotMiddle.getY());
//		System.out.println("Dif. on x-axis" + (robotMiddle.getX() - dest.getX()));
//		System.out.println("Dif. on y-axis" + (robotMiddle.getY() - dest.getY()));

//		if(robotMiddle.getX() < dest.getX()+error_margin && robotMiddle.getX() > dest.getX()-error_margin &&
//		 robotMiddle.getY() < dest.getY()+error_margin && robotMiddle.getY() > dest.getY()-error_margin
//				) return true;

		if (robotMiddle.getX() < dest.getX() + d && robotMiddle.getX() > dest.getX() - d
				&& robotMiddle.getY() < dest.getY() + d && robotMiddle.getY() > dest.getY() - d)
			return true;
		return false;
	}

	public void CommunicateToServerPickup() {
		CommunicateToServer("0F:11;0R:0;0S:0;0B:true;");
	}

	// picks up the nearest ball
	public void NearestSafeBallPickupAlgorithm(List<Point> Balls, double counter) {
		// if the list isn't empty
		Point nearestBall = findNearestBall(robotFront, Balls);
		
		// Point nearestBall = findNearestBall(robotMiddle, safeBalls);
		while (!checkIfCoordsNear(robotFront, nearestBall, 12)) // old 12
		{
			
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, nearestBall);

			CommunicateToServer(commandToSend);

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
			
			CommunicateToServer("0F:1;0R:300;0S:200;0B:true;");
			while (RC.robotExecuting) {
				System.out.print("");
			}
			
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
			CommunicateToServer("0F:2;0R:200;0S:200;0B:true;"); //Bak lidt
			while (RC.robotExecuting) {
				System.out.print("");
			}

		} else if (cornerPickupPoints.contains(nearestBall)) {
			
			CommunicateToServer("0F:1;0R:300;0S:200;0B:true;");
			while (RC.robotExecuting) {
				System.out.print("");
			}
			
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
			CommunicateToServer("0F:2;0R:300;0S:200;0B:true;");	//Bak lidt
			while (RC.robotExecuting) {
				System.out.print("");
			}
		} else if (xPickupPoints.contains(nearestBall)) {
			
			CommunicateToServer("0F:1;0R:300;0S:200;0B:true;");
			while (RC.robotExecuting) {
				System.out.print("");
			}
			
			nearestBall = xBalls.get(xPickupPoints.indexOf(nearestBall));
			while (!checkIfCoordsNear(robotFront, nearestBall, 10)) {
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

	}

	// heads for goal safe spot and unloads
	public void HeadForGoalAndUnload() {
		// task 1 is to get to goalspot 1.
		// we want to keep going until we have reached that point
		
		// loop until we reached point one
		while (!checkIfCoordsNear(robotFront, goalPointOne, 20)) {
			String commandToSend = Calculator.getDir(robotFront, robotMiddle, goalPointOne);
			CommunicateToServer(commandToSend);
			System.out.println("Going For One ----------------------------------------");
			while (RC.robotExecuting) {
				System.out.print("");
			}
			
			ImageRec.runImageRec();
			GetImageInfo();
		}
		
		CommunicateToServer("0F:1;0R:300;0S:200;0B:true;");
		while (RC.robotExecuting) {
			System.out.print("");
		}
		
		// BENJAMIN TILFØJELSE
		//ImageRec.unload = true;
		ImageRec.runImageRec();
		GetImageInfo();
		
		if(allBalls.size() == 0) {
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
			}
			String command = Calculator.turn(robotFront, robotMiddle, goalMiddle);
			CommunicateToServer(command);
		
			while (RC.robotExecuting) {
				System.out.print("");
			}
			
			CommunicateToServer("0F:1;0R:140;0S:200;0B:true;"); // Kør lidt frem
			while (RC.robotExecuting) {
				System.out.print("");
			}
			////////////////////////////KLAR TIL UNLOAD////////////////////////////
			CommunicateToServer("0F:13;0R:600;0S:100;0B:true"); // K?r armen helt op!
			while (RC.robotExecuting) {
				System.out.print("");
			}
			/////////HVIS DER ER MINDRE END 6 BOLDE
			if(firstUnloadDone && ballsOnMap < 6) {
				for (int i = 0; i < ballsOnMap+2; i++) {
					CommunicateToServer("0F:17;0R:0;0S:0;0B:true;");
					while (RC.robotExecuting) {
						System.out.print("");
					}
//					CommunicateToServer("0F:14;0R:0;0S:0;0B:true;");
//					while (RC.robotExecuting) {
//						System.out.print("");
//					}
//					CommunicateToServer("0F:20;0R:0;0S:0;0B:true;");
//					while (RC.robotExecuting) {
//						System.out.print("");
//					}
				}
				/*// HAIL MARY
				CommunicateToServer("0F:2;0R:180;0S:200;0B:true;"); // Kør lidt tilbage
				while (RC.robotExecuting) {
					System.out.print("");
				}
				*/
			}
			/////////HVIS DER ER MERE END 6 BOLDE
			else {
//				for (int i = 0; i < 11; i++) {
//					CommunicateToServer("0F:17;0R:0;0S:0;0B:true;");
//					while (RC.robotExecuting) {
//						System.out.print("");
//					}
//					CommunicateToServer("0F:14;0R:0;0S:0;0B:true;");
//					while (RC.robotExecuting) {
//						System.out.print("");
//					}
//					CommunicateToServer("0F:20;0R:0;0S:0;0B:true;");
//					while (RC.robotExecuting) {
//						System.out.print("");
//					}
//				}
				CommunicateToServer("0F:12;0R:0;0S:0;0B:true;"); // Smid bolde ud
				while (RC.robotExecuting) {
					System.out.print("");
				}
				
//				CommunicateToServer("0F:19;0R:0;0S:0;0B:true;"); // Smid de sidste bolde ud
//				while (RC.robotExecuting) {
//					System.out.print("");
//				}
				
				CommunicateToServer("0F:18;0R:180;0S:200;0B:true;"); // Kør arm lidt ned
				while (RC.robotExecuting) {
					System.out.print("");
				}
				
				CommunicateToServer("0F:20;0R:180;0S:200;0B:true;"); // Kør arm lidt op
				while (RC.robotExecuting) {
					System.out.print("");
				}
				
				/*// HAIL MARY
				CommunicateToServer("0F:2;0R:180;0S:200;0B:true;"); // Kør lidt tilbage
				while (RC.robotExecuting) {
					System.out.print("");
				}
				
				CommunicateToServer("0F:3;0R:500;0S:600;0B:true;"); // Drej rundt
				while (RC.robotExecuting) {
					System.out.print("");
				}
				*/
				
				// ImageRec.runImageRec();
				// GetImageInfo();
				
				// find punkt nr. 2 igen
				/*
				while (!checkIfCoordsNear(robotFront, goalPointTwo, 20)) {
					String commandToSend = Calculator.getDir(robotFront, robotMiddle, goalPointTwo);
					CommunicateToServer(commandToSend);
					System.out.println("Going For Two ----------------------------------------");
					while (RC.robotExecuting) {
						System.out.print("");
					}
					ImageRec.runImageRec();
					GetImageInfo();
				}
				*/
				
				/*// HAIL MARY
				CommunicateToServer("0F:2;0R:100;0S:200;0B:true;"); // Kør lidt tilbage
				while (RC.robotExecuting) {
					System.out.print("");
				}
				
				
				command = Calculator.turn(robotFront, robotMiddle, goalMiddle);
				CommunicateToServer(command);
				while (RC.robotExecuting) {
					System.out.print("");
				}
				
				CommunicateToServer("0F:19;0R:0;0S:0;0B:true;"); // Smid bolde ud igen (2. gang)
				while (RC.robotExecuting) {
					System.out.print("");
				}
				
			}
				
			// firstUnloadDone = true;
			// shotOnGoal = true;
			
			
			//Kører til connectionpoint
			while (!checkIfCoordsNear(robotFront, newConnectionpoints.get(2), 30)) // old 12
			{
				String commandToSend = Calculator.getDir(robotFront, robotMiddle, newConnectionpoints.get(2));
				CommunicateToServer(commandToSend);
				while (RC.robotExecuting) {
					System.out.print("");
				}
				ImageRec.runImageRec();
				GetImageInfo();
			}
			ballsOnMap = allBalls.size();
		}
		CommunicateToServer("0F:18;0R:500;0S:600;0B:true;"); // Arm lidt ned igen
		while (RC.robotExecuting) {
			System.out.print("");
		} */
			}
		}
	}

	public boolean isPathClear(Point robotMiddle, Point dest, Point xMiddle) {
		double Slope = (dest.getY() - robotMiddle.getY()) / (dest.getX() - robotMiddle.getX());
		System.out.println("Slope: " + Slope);
		double Intercept = robotMiddle.getY() - Slope * robotMiddle.getX();
		System.out.println("Intercept" + Intercept);
		
		double distanceFromPathToCross = Math.abs(Slope * xMiddle.getX() + Intercept - xMiddle.getY())/Math.sqrt(Math.pow(Slope, 2) + 1);
		System.out.println("Robotmiddle: " + robotMiddle.getX() + ", " + robotMiddle.getY() + "\n"
				+ "robotFront: " + robotFront.getX() + ", " + robotFront.getY() + "\n"
						+ "Cross: " + xMiddle.getX() + ", " + xMiddle.getY() + "\n"
								+ "dest: " + dest.getX() + ", " + dest.getY() + "\n"
									+ "" +"Distance from cross: " + distanceFromPathToCross);
		///CHECK FOR MODSAT RETNING
		boolean otherWay = false;
		
		if((robotMiddle.getY()-dest.getY())*(robotMiddle.getY()-xMiddle.getY()) < 0 && (robotMiddle.getX()-dest.getX())*(robotMiddle.getX()-xMiddle.getX()) < 0) {
			otherWay = true;
		}
		
		
		if (RADIUS < distanceFromPathToCross || Calculator.calc_Dist(robotMiddle, xMiddle)
				>Calculator.calc_Dist(robotMiddle, dest) || otherWay) return true;
		
		else return false;
	}
	
	public List<Point> BallsWithDirectPathFunc(Point robotMiddle, List<Point> balls) {
		List<Point> ballsWithDirectPath = new ArrayList<Point>();
		// for each ball, check its path and put it into a list if no obstacles
		for (Point ballPoint : balls) {
			if (isPathClear(robotMiddle, ballPoint, xCenter)) {
				System.out.println("is path clear? YES IT IS");
				ballsWithDirectPath.add(ballPoint);
			}
			//keyb.next();
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
		// Kï¿½r arm lidt ned
//		keyb.next();
		CommunicateToServer("0F:14;0R:0;0S:0;0B:true");
		while (RC.robotExecuting) {
			System.out.print("");
		}
//		keyb.next();
		// Bak lidt
		CommunicateToServer("0F:2;0R:150;0S:100;0B:true");
		while (RC.robotExecuting) {
			System.out.print("");
		}
//		keyb.next();
		// Arm helt ned og op
		CommunicateToServer("0F:15;0R:0;0S:0;0B:true");
		while (RC.robotExecuting) {
			System.out.print("");
		}
//		keyb.next();
		// Bak lidt
		CommunicateToServer("0F:2;0R:150;0S:100;0B:true");
		while (RC.robotExecuting) {
			System.out.print("");
		}
//		keyb.next();

	}

}
