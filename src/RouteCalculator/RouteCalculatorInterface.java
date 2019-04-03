package RouteCalculator;

public interface RouteCalculatorInterface {
	
	public double calc_Dist(int endX, int endY, int startX, int startY);
	
	public double calc_Angle(int x1, int y1, int x2, int y2, int startX, int startY);
	
	public String[] getDir(PointInGrid a, PointInGrid b, PointInGrid c); //pos: current position, dest: destnation

}
