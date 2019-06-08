package Logic;

import RouteCalculator.RouteCalculator;
import RouteCalculator.RouteCalculatorInterface;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import RouteCalculator.PointInGrid;

public class RouteLogic implements IRouteLogic, Runnable {
	
	private int checkpoint;
	private List<PointInGrid> coordinates;
	private RouteCalculatorInterface Calculator;
	private PointInGrid robotMiddle, robotFront; 
	private List<PointInGrid> Balls, ConnectionPoints; 
	private int[][] ImageGrid;
	private final int obstacle = 1;
	
	public RouteLogic() {
		this.Calculator = new RouteCalculator();
	}
	public RouteLogic(PointInGrid robotMiddle, PointInGrid robotFront, List<PointInGrid> Balls, List<PointInGrid> ConnectionPoints, int[][] ImageGrid) 
	{
		this.robotMiddle = robotMiddle;
		this.robotFront = robotFront;
		this.Balls = Balls;
		this.ConnectionPoints = ConnectionPoints;
		this.ImageGrid = ImageGrid;
		this.Calculator = new RouteCalculator();
		
		double dist = 10000;
		PointInGrid closestPoint = null;

		for (PointInGrid connection : ConnectionPoints) {
			if (Calculator.calc_Dist(robotMiddle, connection) < dist) {
			dist = Calculator.calc_Dist(robotMiddle, connection);
			closestPoint = connection;
			}
		}
		
	}
	
	
	public void running() {
		//findNearestBall(Robot, BallPoints)
		
	}
	
	/***
	 * finds all points between to points
	 * @param pos
	 * @param dest
	 * @return
	 */
	public List<PointInGrid> pointsOnRoute(PointInGrid pos, PointInGrid dest) {
		
		coordinates = new ArrayList<PointInGrid>();
		
		double Slope = (dest.getX() - pos.getX()) / (dest.getY() - pos.getY());
		double Intercept = pos.getX() - Slope * pos.getY();
		
		//TODO SOMEHOW TRIGGER checkpoint case 1,2,3 and 4? What if the robot is not on one of those?
		//ikke initialiseret nogle steder, så altid ende i default?
		switch(checkpoint) {
			case 1: //runs while start y is less than end y.
					//bottom to top (both sides)
				for (int y = (int) pos.getY(); y <= (int) dest.getY(); y++) {
					int x = (int) (Slope * y + Intercept);
					coordinates.add(new PointInGrid(x, y));
				}
				break;
			case 2: //runs while start x is less than end x.
					//right to left (both lower and upper side)
				//was ...; x <= (int)....
				for (int x = (int) pos.getX(); x >= (int) dest.getX(); x--) {
					int y = (int) ((x-Intercept)/Slope);
					coordinates.add(new PointInGrid(x, y));
				}
				break;
			case 3: //runs while start y is greater than end y
					//top to bottom (both sides)
				for (int y = (int) pos.getY(); y >= (int) dest.getY(); y--) {
					int x = (int) (Slope * y + Intercept);
					coordinates.add(new PointInGrid(x, y));
				}
				break;
			case 4: //runs while start x is less than end x
					//left to right (both lower and upper side)
				for (int x = (int) pos.getX(); x <= (int) dest.getX(); x++) {
					int y = (int) ((x-Intercept)/Slope);
					coordinates.add(new PointInGrid(x, y));
				}
				break;
			default: //purpose? will it ever get triggered?
				coordinates = pointsOnRoute(pos, dest);
		}
		
		return coordinates;
	}
	
	/***
	 * Evaluates all points in route.
	 * to see if there is an angle on the route towards the nearest ball from
	 * the side within the 85-95 degree angle.
	 */
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
		//from point to nearestball
		//check entire route for obstacles
		for (PointInGrid p : pointsOnRoute(Point, nearestBall))
		{
			if(ImageGrid[(int) p.getX()][(int) p.getY()] == obstacle)
			{
				Point = null;
			}
		}
		return Point;
	}

	/***
	 * searches through all balls in list and reevalutes if one is closer than the other
	 * return: the point of the closest ball (nomatter its position)
	 */
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

	/***
	 * 
	 * Robot: The middle of the robot
	 * conPoint: the front of the robot
	 */
	@Override
	public void Drive(PointInGrid conPoint, PointInGrid Robot, PointInGrid nextCornor, PointInGrid nearestBall) {		
		
		//this gets triggered if the EvalRoute could 
		//not find a point in which there was
		//an appropriate angle towards the ball without an obstacle in between
		if (EvalRoute(Robot, nextCornor, nearestBall) == null)
			//will compile the string to send to the robot so that the robot can drive
			//to the next corner
			Calculator.getDir(conPoint, Robot, nextCornor);
		
		//if there was a succesfull point with angle then
		//create the route 
		else
			Calculator.getDir(conPoint, Robot, EvalRoute(Robot, nextCornor, nearestBall));
			//Calculator.getDir(conPoint, Robot, EvalRoute(Robot, nextCornor, findNearestBall(Robot, BallPoints)));
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
