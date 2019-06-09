package RouteCalculator;

public class RouteCalculator implements RouteCalculatorInterface  {

	public static int TrackLenght = 1920;
	public static int TrackWidth = 1080;

	private PointInGrid returnPoint;

	PointInGrid [] checkPoints = {	new PointInGrid(TrackWidth/4, TrackLenght/4),
			new PointInGrid(TrackWidth/4, 3*(TrackLenght/4)),
			new PointInGrid(3*(TrackWidth/4), TrackLenght/4),
			new PointInGrid(3*(TrackWidth/4), 3*(TrackLenght/4)) };

	/**
	 * returns pixels in forms of double????
	 */
	@Override
	public double calc_Dist(PointInGrid posPoint, PointInGrid destPoint) {
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
	public double calc_Angle(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint) {
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
	public String getDir(PointInGrid robotFront, PointInGrid robotMiddle, PointInGrid destPoint) {
		StringBuilder str = new StringBuilder();
		
		//readies string parts
		String OF = "0F:0;"; //F is Forward
		String OG = "0G:0;"; //G is grader (degrees)
		String OS = "0S:0;"; //S is speed
		String LR = "LR:0;"; //LR is left rotate
		String RR = "RR:0;"; //RR is right rotate
		String OB = "0B:false"; // B is boolean 

		//calculates angle for robot to turn
		double angle = calc_Angle(robotFront, robotMiddle, destPoint);
		System.out.println("--------- NOT ABS ----------");
		System.out.println("ANGLE: "+ angle);
		System.out.println("ANGLE2: "+ (360 - angle)+"\n");

		System.out.println("--------- WITH ABS ---------");
		System.out.printf("ANGLE: %.2f\n", Math.abs(angle));
		System.out.printf("ANGLE2: %.2f\n", (360 - Math.abs(angle)));

		// If angle > 0: Turn right, else if angle < 0: Turn left

		//calculates the distance the robot needs to drive
		double dist = calc_Dist(robotMiddle, destPoint);
		System.out.printf("Distance: %.2f\n\n", dist);

		//
		if (angle > 10) {
			OF = "0F:4;"; //forward 4
			RR = "RR:"+Math.round(angle)+";"; //rotate right
		} else if (angle < -10) {
			OF = "0F:3;"; //forward 3
			LR = "LR:"+Math.round(Math.abs(angle))+";"; //rotate left
		} else if (angle >= -10 && angle <= 10) { 
			OF = "0F:1;"; //forward is 1
			if (dist > 300)
				OS = "0S:150;"; //speed is 50
			else OS = "0S:50;"; //speed is 50
		}

		str.append(OF);
		str.append(OG);
		str.append(OS);
		str.append(LR);
		str.append(RR);
		str.append(OB);

		String COMMAND = str.toString();

		System.out.println("COMMAND: "+ COMMAND);

		return COMMAND;
	}

	@Override
	public String goToBall(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint) {
		this.returnPoint = posPoint;
		return getDir(conPoint, posPoint, destPoint);
	}

	@Override
	public String returnToFix(PointInGrid conPoint, PointInGrid posPoint) {
		return getDir(conPoint, posPoint, this.returnPoint);
	}

	
}
