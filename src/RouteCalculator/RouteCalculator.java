package RouteCalculator;

import java.awt.Point;

public class RouteCalculator implements RouteCalculatorInterface  {

	public static int TrackLenght = 1920;
	public static int TrackWidth = 1080;

	private Point returnPoint;

	Point [] checkPoints = {	new Point(TrackWidth/4, TrackLenght/4),
			new Point(TrackWidth/4, 3*(TrackLenght/4)),
			new Point(3*(TrackWidth/4), TrackLenght/4),
			new Point(3*(TrackWidth/4), 3*(TrackLenght/4)) };

	/**
	 * returns pixels in forms of double????
	 */
	@Override
	public double calc_Dist(Point posPoint, Point destPoint) {
		double colDist = destPoint.getX()-posPoint.getX();
		double rowDist = destPoint.getY()-posPoint.getY();

		double SqHyp = Math.pow(colDist, 2) + Math.pow(rowDist, 2);
		double hypDist = Math.sqrt(SqHyp);


		System.out.println("Dist_calc: " + Math.pow(colDist, 2) + " + "+ Math.sqrt(Math.pow(rowDist, 2)));
		return hypDist;
	}

	/**
	 * 
	 */
	@Override
	public double calc_Angle(Point conPoint, Point posPoint, Point destPoint) {
		double destRow = destPoint.getX(), destCol = destPoint.getY();
		double posRow = posPoint.getX(), posCol = posPoint.getY();
		double conRow = conPoint.getX(), conCol = conPoint.getY();
		double angle1 = Math.atan2((conRow - posRow), (conCol - posCol)) * 180/Math.PI;
		double angle2 = Math.atan2((destRow - posRow), (destCol - posCol)) * 180/Math.PI;
		System.out.println("ControlAngle: " + angle1);
		System.out.println("DestAngle: " + angle2);
		//System.out.println("angle3: " + (angle2 - angle1) + "\n");
		double angle = angle2 - angle1;
			return angle;
	}
	/**
	 * conpoint = front of robot
	 * posPoint = middleofRobot
	 * destPoint = where we are going
	 */
	@Override
	public String getDir(Point robotFront, Point robotMiddle, Point destPoint) {
		StringBuilder str = new StringBuilder();
		
		//readies string parts
		String OF = "0F:0;"; //F is FunctionInt
		String OS = "0S:0;"; //S is speed
		String OR = "0R:0;"; //LR is left rotate
//		String RR = "LR:0;"; //LR is left rotate
//		String RR = "RR:0;"; //RR is right rotate
		String OB = "0B:false"; // B is boolean 

		//calculates angle for robot to turn
		double angle = -calc_Angle(robotFront, robotMiddle, destPoint)*5;
//		double angle = calc_Angle(robotFront, robotMiddle, destPoint);
//		System.out.println("--------- NOT ABS ----------");
//		System.out.println("ANGLE: "+ angle);
//		System.out.println("ANGLE2: "+ (360 - angle)+"\n");
//
//		System.out.println("--------- WITH ABS ---------");
//		System.out.printf("ANGLE: %.2f\n", Math.abs(angle));
//		System.out.printf("ANGLE2: %.2f\n", (360 - Math.abs(angle)));

		// If angle > 0: Turn right, else if angle < 0: Turn left

		//calculates the distance the robot needs to drive
		double dist = calc_Dist(robotMiddle, destPoint)*5.81;
//		double dist = calc_Dist(robotMiddle, destPoint);
		System.out.printf("Distance: %.2f\n\n", dist);

		System.out.println("Angle : " + angle);
		//
		if (angle > 10) {
			OF = "0F:4;"; //function 4 (right)
			OR = "0R:"+Math.round(angle)+";"; //rotate right
			OS = "0S:100;";
		} else if (angle < -10) {
			OF = "0F:3;"; //function 3 (left)
			OR = "0R:"+Math.round(Math.abs(angle))+";"; //rotate left
			OS = "0S:100;";
		} else if (angle >= -10 && angle <= 10) {
			OF = "0F:1;"; //forward is 1
			if (dist > 300)
			{
				//TODO 
				OR = "0R:" + dist + ";"; //rotate 200 on both motors
				//OR = "0R:200;"; //rotate 200 on both motors
				OS = "0S:150;"; //speed is 150
			}
			else {
				OS = "0S:150;"; //speed is 50
//				OR = "0R:200;"; //Hardcoded 200 rotations on motor
				OR = "0R:" + dist + ";"; //rotate 200 on both motors
			}
		}

		str.append(OF);
		str.append(OS);
		str.append(OR);
//		str.append(LR);
//		str.append(RR);
		str.append(OB);

		String COMMAND = str.toString();

		System.out.println("COMMAND: "+ COMMAND);

		return COMMAND;
	}

	@Override
	public String goToBall(Point conPoint, Point posPoint, Point destPoint) {
		this.returnPoint = posPoint;
		return getDir(conPoint, posPoint, destPoint);
	}

	@Override
	public String returnToFix(Point conPoint, Point posPoint) {
		return getDir(conPoint, posPoint, this.returnPoint);
	}

	
}
