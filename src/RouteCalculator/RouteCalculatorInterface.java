package RouteCalculator;

public interface RouteCalculatorInterface {

	/**
	 * @param posPoint the position of rotation center
	 * @param destPoint the position of target
	 * @return The distance between start and end (how is it represented in numbers based on the grid scale?)
	 */
	public double calc_Dist(PointInGrid posPoint, PointInGrid destPoint);


	/**
	 * @param x1 Row coordinate for controlPoint
	 * @param y1 Col coordinate for controlPoint
	 * @param x2 Row coordinate for target
	 * @param y2 Col coordinate for target
	 * @param startX Row coordinate for rotation center
	 * @param startY Col coordinate for rotation center
	 * @return The angle the robot shall turn (in degrees)
	 */
	public double calc_Angle(double x1, double y1, double x2, double y2, double startX, double startY);

	/**
	 * @param conPoint ControlPoint in from of robot
	 * @param posPoint Rotation center of robot
	 * @param destPoin	Position of target table tennis ball
	 * @return String containing the command for  the robot
	 */
	public String getDir(PointInGrid conPoint, PointInGrid posPoint, PointInGrid destPoint);


	/**
	 * @param conPoint ControlPoint in from of robot
	 * @param posPoint Rotation center of robot
	 * @return String containing the command for  the robot
	 */
	public String goToNearestCheckpoint(PointInGrid conPoint, PointInGrid posPoint);

	/**
	 * @param conPoint ControlPoint in from of robot
	 * @param pospoint Rotation center of robot
	 * @param destPoint	Position of target point
	 * @return String containing the command for  the robot
	 */
	public String goToNextCheckpoint(PointInGrid conPoint, PointInGrid pospoint, PointInGrid destPoint);

	/**
	 *
	 * @param point A given point somewhere on the track
	 * @return Integer referring to a specific part of the track
	 */
	public int setQuadrant(PointInGrid point);

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
