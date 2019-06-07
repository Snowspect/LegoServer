package RouteCalculator;

public interface RouteCalculatorInterface {

	/**
	 * @param posPoint the position of rotation center
	 * @param destPoint the position of target
	 * @return The distance between start and end (how is it represented in numbers based on the grid scale?)
	 */
	public double calc_Dist(PointInGrid posPoint, PointInGrid destPoint);


	/**
	 * 
	 * @param conPoint ControlPoint in from of robot
	 * @param posPoint Rotation center of robot
	 * @param destPoin	Position of target table tennis ball
	 * @return The angle the robot shall turn (in degrees)
	 */
	public double calc_Angle(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint);

	/**
	 * @param conPoint ControlPoint in from of robot
	 * @param posPoint Rotation center of robot
	 * @param destPoin	Position of target table tennis ball
	 * @return String containing the command for  the robot
	 */
	public String getDir(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint);

	/**
	 *
	 * @param conPoint ControlPoint in from of robot
	 * @param pospoint Rotation center of robot
	 * @param destPoint	Position of target point
	 * @return String containing the command for  the robot
	 */
	public String goToBall(PointInGrid conPoint, PointInGrid pospoint, PointInGrid destPoint);

	/**
	 *
	 * @param conPoint ControlPoint in from of robot
	 * @param pospoint Rotation center of robot
	 * @return String containing the command for  the robot
	 */
	public String returnToFix(PointInGrid conPoint, PointInGrid posPoint);

}
