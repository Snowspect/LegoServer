package RouteCalculator;

public interface RouteCalculatorInterface {
	
	/**
	 * @param endX: Row coordinate for target
	 * @param endY: Col coordinate for target
	 * @param startX: Row coordinate for rotation center
	 * @param startY: Col coordinate for rotation center
	 * @return: The distance between start and and
	 */
	public double calc_Dist(int endX, int endY, int startX, int startY);
	
	
	/**
	 * @param x1: Row coordinate for controlpoint
	 * @param y1: Row coordinate for target
	 * @param x2: Col coordinate for controlPoint
	 * @param y2: Col coordinate for target
	 * @param startX: Row coordinate for rotation center
	 * @param startY: Col coordinate for rotation center
	 * @return: The angle the robot shall turn (in degrees)
	 */
	public double calc_Angle(int x1, int y1, int x2, int y2, int startX, int startY);
	
	/**
	 * @param conPoint: Controlpoint in from of robot
	 * @param pospoint: Rotation center of robot
	 * @param destPoin:	Position of target tabletennis ball
	 * @return: String-array that contains the command that will be sent to the robot
	 * "0F:2;0G:200;0S:300;LR:40;RR:50;0B:true" 
		0F = function
		0G = grades
		OS = Speed
		LR = LeftRotation(grader)
		RR = RightRotation(grader)
		0B = boolean
	 */
	public String[] getDir(PointInGrid conPoint, PointInGrid pospoint, PointInGrid destPoint); //pos: current position, dest: destnation

}
