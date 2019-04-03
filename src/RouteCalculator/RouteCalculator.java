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
	public double calc_Angle(int x1, int y1, int x2, int y2, int startX, int startY) {
		double angle1 = Math.atan2((x1 - startX), (y1 - startY)) * 180/Math.PI;
		double angle2 = Math.atan2(x2 - startX, y2 - startY) * 180/Math.PI;
		System.out.println("angle1: " + angle1);
		System.out.println("angle2: " + angle2);
		System.out.println("angle3: " + (angle2 - angle1) + "\n");
		double angle = angle2 - angle1;
			return angle;

	}

	@Override
	public void getDir(PointInGrid a, PointInGrid b, PointInGrid c) {
		int destRow = b.getX(), destCol = b.getY();
		int posRow = a.getX(), posCol = b.getY();
		int conRow = c.getX(), conCol = b.getY();
		
		double angle = calc_Angle(conCol, conRow, destCol, destRow, posCol, posRow);
		
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
		System.out.println("Distance: " + dist);
	}
	
	

	
}
