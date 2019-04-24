package RouteCalculator;

public class RouteCalculator implements RouteCalculatorInterface  {
	
	private static int TrackLenght = 1920;
	private static int TrackWidth = 1080;
	
	/**
	 * calculates the distance between two points.
	 * Currently the distance is returned as: xxx
	 */
	@Override
	public double calc_Dist(PointInGrid posPoint, PointInGrid destPoint) {
		int colDist = destPoint.getX()-posPoint.getX();
		int rowDist = destPoint.getY()-posPoint.getY();
		
		double SqHyp = Math.pow(colDist, 2) + Math.pow(rowDist, 2);
		double hypDist = Math.sqrt(SqHyp);
		
		
		System.out.println("Dist_calc: " + Math.pow(colDist, 2) + " + "+ Math.sqrt(Math.pow(rowDist, 2)));
		return hypDist;
	}

	/**
	 * Calculates angle between two points
	 * The angle is returned on a 360 degree scale.
	 */
	@Override
	public double calc_Angle(int conX, int conY, int endX, int endY, int startX, int startY) {
		double angle1 = Math.atan2((conX - startX), (conY - startY)) * 180/Math.PI;
		double angle2 = Math.atan2(endX - startX, endY - startY) * 180/Math.PI;
		System.out.println("ControlAngle: " + angle1);
		System.out.println("DestAngle: " + angle2);
		//System.out.println("angle3: " + (angle2 - angle1) + "\n");
		double angle = angle2 - angle1;
			return angle;

	}

	
	@Override
	public String getDir(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint) {
		StringBuilder str = new StringBuilder();
		int destRow = destPoint.getX(), destCol = destPoint.getY();
		int posRow = posPoint.getX(), posCol = posPoint.getY();
		int conRow = conPoint.getX(), conCol = conPoint.getY();
		
		String OF = "0F:0;";
		String OG = "0G:0;";
		String OS = "0S:0;";
		String LR = "LR:0;";
		String RR = "RR:0;";
		String OB = "0B:false";
		
		double angle = calc_Angle(conRow, conCol, destRow, destCol, posRow, posCol);
		System.out.println("--------- NOT ABS ----------");
		System.out.println("ANGLE: "+ angle);
		System.out.println("ANGLE2: "+ (360 - angle)+"\n");
		
		System.out.println("--------- WITH ABS ---------");
		System.out.printf("ANGLE: %.2f\n", Math.abs(angle));
		System.out.printf("ANGLE2: %.2f\n", (360 - Math.abs(angle)));
		
		// If angle > 0: Turn right, else if angle < 0: Turn left
		
		double dist = calc_Dist(posPoint, destPoint);
		System.out.printf("Distance: %.2f\n\n", dist);
		
		if (angle > 0) {
			OF = "0F:4;";
			RR = "RR:"+Math.round(angle)+";";
		} else if (angle < 0) {
			OF = "0F:3;";
			LR = "LR:"+Math.round(Math.abs(angle))+";";
		} else if (angle == 0) {
			OF = "0F:1;";
			if (dist > 300)
				OS = "0S:150;";
			else OS = "0S:50;";
		}
		
		str.append(OF);
		str.append(OG);
		str.append(OS);
		str.append(LR);
		str.append(RR);
		str.append(OB);
		
		String COMMAND = str.toString();
		
		System.out.println("COMMAND: "+COMMAND);
		
		return COMMAND;
	}

	@Override
	public String goToNearestCheckpoint(PointInGrid conPoint, PointInGrid posPoint) {
		PointInGrid [] checkPoints = {	new PointInGrid(TrackWidth/4, TrackLenght/4),
										new PointInGrid(TrackWidth/4, 3*(TrackLenght/4)),
										new PointInGrid(3*(TrackWidth/4), TrackLenght/4),
										new PointInGrid(3*(TrackWidth/4), 3*(TrackLenght/4)) };
		
		String Command = null;
		
		if (posPoint.getX() <= TrackWidth/2 && posPoint.getY() <= TrackLenght/2)
			Command = getDir(conPoint, posPoint, checkPoints[0]);
		
		else if (posPoint.getX() <= TrackWidth/2 && posPoint.getY() > TrackLenght/2)
			Command = getDir(conPoint, posPoint, checkPoints[1]);
		
		else if (posPoint.getX() > TrackWidth/2 && posPoint.getY() <= TrackLenght/2)
			Command = getDir(conPoint, posPoint, checkPoints[2]);
		
		else if (posPoint.getX() > TrackWidth/2 && posPoint.getY() > TrackLenght/2)
			Command = getDir(conPoint, posPoint, checkPoints[3]);
		
		return Command;
	}
	
	

	
}
