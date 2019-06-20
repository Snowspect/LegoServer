package Logic;

import java.awt.Point;
import java.util.List;

import RouteCalculator.PointInGrid;

public interface IRouteLogic {
	/**
	 * 
	 * @param Robot Location of the robot
	 * @param BallPoints List consisting if the locations for all the balls
	 * @return Location of the nearest ball
	 */
	public Point findNearestBall(Point Robot, List<Point> BallPoints);

}
