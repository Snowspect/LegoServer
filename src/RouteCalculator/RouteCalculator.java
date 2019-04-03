package RouteCalculator;

public class RouteCalculator implements RouteCalculatorInterface  {
	
	@Override
	public double calc_Dist(int endX, int endY, int startX, int startY) {
		int colDist = endX-startX;
		int rowDist = endY-startY;
		
		double hypDist = (int) Math.sqrt(Math.pow(colDist, 2) + Math.pow(rowDist, 2));

		return hypDist;
	}

	@Override
	public double calc_Angle(int conX, int conY, int endX, int endY, int startX, int startY) {
		double angle1 = Math.atan2((conX - startX), (conY - startY)) * 180/Math.PI;
		double angle2 = Math.atan2(endX - startX, endY - startY) * 180/Math.PI;
		System.out.println("angle1: " + angle1);
		System.out.println("angle2: " + angle2);
		//System.out.println("angle3: " + (angle2 - angle1) + "\n");
		double angle = angle2 - angle1;
			return angle;

	}

	@Override
	public String[] getDir(PointInGrid posPoint, PointInGrid destPoint, PointInGrid conPoint) {
		String[] dir = new String[2];
		int destRow = destPoint.getX(), destCol = destPoint.getY();
		int posRow = posPoint.getX(), posCol = posPoint.getY();
		int conRow = conPoint.getX(), conCol = conPoint.getY();
		
		double angle = calc_Angle(conRow, conCol, destRow, destCol, posRow, posCol);
		dir[0] = "\"" + angle + "\"";
		System.out.println("ANGLE: "+angle+"\n");
//		
//		if (angle > 0) {
//			SendCommand(RIGHT);
//		} else if (angle < 0) {
//			SendCommand(LEFT);
//		} else {
//			//Kør lige ud eller bagud
//		}
		
		double dist = calc_Dist(destRow, destCol, posCol, posRow);
		dir[1] = "\"" + dist + "\"";
		System.out.println("Distance: " + dist);
		
		return dir;
	}
	
	

	
}
