package RouteCalculator;

public class RouteCalculator implements RouteCalculatorInterface  {
	
	public static int TrackLenght = 1920;
	public static int TrackWidth = 1080;
	
	private PointInGrid returnPoint;
	
	PointInGrid [] checkPoints = {	new PointInGrid(TrackWidth/4, TrackLenght/4),
			new PointInGrid(TrackWidth/4, 3*(TrackLenght/4)),
			new PointInGrid(3*(TrackWidth/4), TrackLenght/4),
			new PointInGrid(3*(TrackWidth/4), 3*(TrackLenght/4)) };
	
	@Override
	public double calc_Dist(PointInGrid posPoint, PointInGrid destPoint) {
		double colDist = destPoint.getX()-posPoint.getX();
		double rowDist = destPoint.getY()-posPoint.getY();
		
		double SqHyp = Math.pow(colDist, 2) + Math.pow(rowDist, 2);
		double hypDist = Math.sqrt(SqHyp);
		
		
		System.out.println("Dist_calc: " + Math.pow(colDist, 2) + " + "+ Math.sqrt(Math.pow(rowDist, 2)));
		return hypDist;
	}

	@Override
	public double calc_Angle(double conRow, double conCol, double destRow, double destCol, double posRow, double posCol) {
		double angle1 = Math.atan2((conRow - posRow), (conCol - posCol)) * 180/Math.PI;
		double angle2 = Math.atan2((destRow - posRow), (destCol - posCol)) * 180/Math.PI;
		System.out.println("ControlAngle: " + angle1);
		System.out.println("DestAngle: " + angle2);
		//System.out.println("angle3: " + (angle2 - angle1) + "\n");
		double angle = angle2 - angle1;
			return angle;

	}

	@Override
	public String getDir(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint) {
		StringBuilder str = new StringBuilder();
		double destRow = destPoint.getX(), destCol = destPoint.getY();
		double posRow = posPoint.getX(), posCol = posPoint.getY();
		double conRow = conPoint.getX(), conCol = conPoint.getY();
		
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
	public String goToBall(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint) {
		this.returnPoint = posPoint;
		return getDir(conPoint, posPoint, destPoint);
	}
	
	@Override
	public String returnToFix(PointInGrid conPoint, PointInGrid posPoint) {
		return getDir(conPoint, posPoint, this.returnPoint);
	}
	
	@Override
	public String goToNearestCheckpoint(PointInGrid conPoint, PointInGrid posPoint) {
		
		int QUAD = setQuadrant(posPoint);
		String Command = null;
		
		if (QUAD == 1)
			Command = getDir(conPoint, posPoint, checkPoints[0]);
		
		else if (QUAD == 2)
			Command = getDir(conPoint, posPoint, checkPoints[1]);
		
		else if (QUAD == 3)
			Command = getDir(conPoint, posPoint, checkPoints[2]);
		
		else if (QUAD == 4)
			Command = getDir(conPoint, posPoint, checkPoints[3]);
		
		return Command;
	}

	@Override
	public String goToNextCheckpoint(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint) {
		System.out.println("Going to checkpoint");
		int QUAD_car = setQuadrant(posPoint), QUAD_ball = setQuadrant(destPoint);
		String Command = null;
		
		if (QUAD_car == 1) {
			
			if (QUAD_ball == 1)
				Command = getDir(conPoint, posPoint, destPoint);
			
			else if (QUAD_ball == 2)
				Command = getDir(conPoint, posPoint, checkPoints[1]);
			
			else if (QUAD_ball == 3)
				Command = getDir(conPoint, posPoint, checkPoints[2]);
			
			else if (QUAD_ball == 4)
				Command = getDir(conPoint, posPoint, checkPoints[2]);
			
		}
		
		else if (QUAD_car == 2) {
			if (QUAD_ball == 1)
				Command = getDir(conPoint, posPoint, checkPoints[0]);
			
			else if (QUAD_ball == 2)
				Command = getDir(conPoint, posPoint, destPoint);
			
			else if (QUAD_ball == 3)
				Command = getDir(conPoint, posPoint, checkPoints[3]);
			
			else if (QUAD_ball == 4)
				Command = getDir(conPoint, posPoint, checkPoints[3]);
		}
		
		else if (QUAD_car == 3) {
			if (QUAD_ball == 1)
				Command = getDir(conPoint, posPoint, checkPoints[0]);
			
			else if (QUAD_ball == 2)
				Command = getDir(conPoint, posPoint, checkPoints[0]);
			
			else if (QUAD_ball == 3)
				Command = getDir(conPoint, posPoint, destPoint);
			
			else if (QUAD_ball == 4)
				Command = getDir(conPoint, posPoint, checkPoints[3]);
		}
		
		else if (QUAD_car == 4) {
			if (QUAD_ball == 1)
				Command = getDir(conPoint, posPoint, checkPoints[1]);
			
			else if (QUAD_ball == 2)
				Command = getDir(conPoint, posPoint, checkPoints[1]);
			
			else if (QUAD_ball == 3)
				Command = getDir(conPoint, posPoint, checkPoints[2]);
			
			else if (QUAD_ball == 4)
				Command = getDir(conPoint, posPoint, destPoint);
		}
		
		return Command;
	}

	@Override
	public int setQuadrant(PointInGrid point) {
		
		
		int QUAD = 1;
		
		if (point.getX() <= TrackWidth/2 && point.getY() <= TrackLenght/2)
			QUAD = 1;
		else if (point.getX() <= TrackWidth/2 && point.getY() > TrackLenght/2)
			QUAD = 2;
		else if (point.getX() > TrackWidth/2 && point.getY() <= TrackLenght/2)
			QUAD = 3;
		else if (point.getX() > TrackWidth/2 && point.getY() > TrackLenght/2)
			QUAD = 4;
		
		return QUAD;
	}

	
}
