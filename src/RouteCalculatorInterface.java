
public interface RouteCalculatorInterface {
	
	public double calc_Dist(int endX, int endY, int startX, int startY);
	
	public double calc_Angle(int x1, int y1, int x2, int y2, int startX, int startY);
	
	public void getDir(int pos, int dest); //pos: current position, dest: destnation

}
