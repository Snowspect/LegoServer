package Logic;

import java.util.List;

import RouteCalculator.PointInGrid;

public interface IRouteLogic {
	
	/**
	 * 
	 * @param Robot Location of the robot
	 * @param nextCornor Location of the next corner on the track 
	 * @param nearestBall Location of the nearest ball
	 * @return Location on the path that gives the robot a 90 degree turn to the nearest ball 
	 */
	public PointInGrid CheckPickupAngleOnRoute(PointInGrid Robot, PointInGrid nextCornor, PointInGrid nearestBall, List<PointInGrid> pointsOnPath);
	
	/**
	 * 
	 * @param Robot Location of the robot
	 * @param BallPoints List consisting if the locations for all the balls
	 * @return Location of the nearest ball
	 */
	public PointInGrid findNearestBall(PointInGrid Robot, List<PointInGrid> BallPoints);

	/**
	 * 
	 * @param conPoint Point right in front of the robot
	 * @param Robot Location of the robot
	 * @param nextCornor Location of the next corner on the track 
	 * @param nearestBall Location of the nearest ball
	 */
	public void Drive(PointInGrid conPoint, PointInGrid Robot, PointInGrid nextCornor, PointInGrid nearestBall);

}
