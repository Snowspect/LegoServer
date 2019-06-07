package Logic;

import RouteCalculator.RouteCalculator;
import RouteCalculator.RouteCalculatorInterface;

import java.util.ArrayList;
import java.util.List;

import RouteCalculator.PointInGrid;

public class RouteLogic implements IRouteLogic {
	
	private int checkpoint;
	private List<PointInGrid> coordinates;
	private RouteCalculatorInterface Calculator;
	
	public RouteLogic() {
		this.Calculator = new RouteCalculator();
	}
	
	public List<PointInGrid> pointsOnRoute(PointInGrid pos, PointInGrid dest) {
		
		coordinates = new ArrayList<PointInGrid>();
		
		double Slope = (dest.getX() - pos.getX()) / (dest.getY() - pos.getY());
		double Intercept = pos.getX() - Slope * pos.getY();
		
		switch(checkpoint) {
			case 1:
				for (int y = (int) pos.getY(); y <= (int) dest.getY(); y++) {
					int x = (int) (Slope * y + Intercept);
					coordinates.add(new PointInGrid(x, y));
				}
				break;
			case 2:
				for (int x = (int) pos.getX(); x <= (int) dest.getX(); x--) {
					int y = (int) ((x-Intercept)/Slope);
					coordinates.add(new PointInGrid(x, y));
				}
				break;
			case 3:
				for (int y = (int) pos.getY(); y >= (int) dest.getY(); y--) {
					int x = (int) (Slope * y + Intercept);
					coordinates.add(new PointInGrid(x, y));
				}
				break;
			case 4:
				for (int x = (int) pos.getX(); x <= (int) dest.getX(); x++) {
					int y = (int) ((x-Intercept)/Slope);
					coordinates.add(new PointInGrid(x, y));
				}
				break;
			default:
				coordinates = pointsOnRoute(pos, dest);
		}
		
		return coordinates;
	}
	
	@Override
	public PointInGrid EvalRoute(PointInGrid Robot, PointInGrid nextPoint, PointInGrid nearestBall) {
		
		PointInGrid Point = null;
		
		for (PointInGrid p : coordinates) {
			double angle = Calculator.calc_Angle(nextPoint, p, nearestBall);
			
			if (angle >= 85 && angle <= 95) {
				Point = p;
				break;
			}
		}
		return Point;
	}

	@Override
	public PointInGrid findNearestBall(PointInGrid Robot, List<PointInGrid> BallPoints) {
		double dist = 10000;
		PointInGrid closestPoint = null;
		
		for (PointInGrid Point : BallPoints) {
			if (Calculator.calc_Dist(Robot, Point) < dist) {
				dist = Calculator.calc_Dist(Robot, Point);
				closestPoint = Point;
			}		
		}
		
		return closestPoint;
	}

	
	@Override
	public void Drive(PointInGrid conPoint, PointInGrid Robot, PointInGrid nextCornor, PointInGrid nearestBall) {
		
		
		
		if (EvalRoute(Robot, nextCornor, nearestBall) == null)
			Calculator.getDir(conPoint, Robot, nextCornor);
		
		else
			Calculator.getDir(conPoint, Robot, EvalRoute(Robot, nextCornor, nearestBall));
		
	}
	
	
	
}
