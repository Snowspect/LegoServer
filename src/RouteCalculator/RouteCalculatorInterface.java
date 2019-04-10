package RouteCalculator;

public interface RouteCalculatorInterface {
	
	/**
	 * @param posPoint the position of rotation center
	 * @param destPoint the position of target
	 * @return: The distance between start and and
	 */
	public double calc_Dist(PointInGrid posPoint, PointInGrid destPoint);
	
	
	/**
	 * @param x1: Row coordinate for controlPoint
	 * @param y1: Row coordinate for target
	 * @param x2: Col coordinate for controlPoint
	 * @param y2: Col coordinate for target
	 * @param startX: Row coordinate for rotation center
	 * @param startY: Col coordinate for rotation center
	 * @return: The angle the robot shall turn (in degrees)
	 */
	public double calc_Angle(int x1, int y1, int x2, int y2, int startX, int startY);
	
	/**
	 * @param conPoint: ControlPoint in from of robot
	 * @param pospoint: Rotation center of robot
	 * @param destPoin:	Position of target tabletennis ball
	 */
	public void getDir(PointInGrid conPoint, PointInGrid pospoint, PointInGrid destPoint); //pos: current position, dest: destnation

}
