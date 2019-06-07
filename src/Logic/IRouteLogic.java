package Logic;

import java.util.List;

import RouteCalculator.PointInGrid;

public interface IRouteLogic {
	
	public PointInGrid EvalRoute(PointInGrid Robot, PointInGrid nextCornor, PointInGrid nearestBall);
	
	public PointInGrid findNearestBall(PointInGrid Robot, List<PointInGrid> BallPoints);

	public void Drive(PointInGrid conPoint, PointInGrid Robot, PointInGrid nextCornor, PointInGrid nearestBall);

}
