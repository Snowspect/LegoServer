package UnitTests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.synth.SynthSpinnerUI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Logic.*;
import RouteCalculator.PointInGrid;

public class RouteLogicTest {

	
	RouteLogic logic;
	List<PointInGrid> ConnectionPoints, Balls;
	public static int TrackLenght = 1920;
	public static int TrackWidth = 1080;
	
	@Before
	public void setUp() throws Exception {
		ConnectionPoints = new ArrayList<PointInGrid>(4);
		ConnectionPoints.add(new PointInGrid(TrackWidth/4, TrackLenght/4));
		ConnectionPoints.add(new PointInGrid(TrackWidth/4, 3*(TrackLenght/4)));
		ConnectionPoints.add(new PointInGrid(3*(TrackWidth/4), TrackLenght/4));
		ConnectionPoints.add(new PointInGrid(3*(TrackWidth/4), 3*(TrackLenght/4)));
		
		Balls = new ArrayList<PointInGrid>();
		Balls.add(new PointInGrid(658, 1685));
		Balls.add(new PointInGrid(295, 450));
		
		logic = new RouteLogic(new PointInGrid(1000, 1800), new PointInGrid(1000, 1798),
				Balls, ConnectionPoints, new int[1080][1920]);
	}

	@After
	public void tearDown() throws Exception {
		logic = null;
	}

	@Test
	public void testFindNearestBall() {
		
		PointInGrid point = logic.findNearestBall(logic.getRobotMiddle(), logic.getBalls());
		assertEquals(Balls.get(0), point);
		
	}
	
	@Test
	public void testPointsOnRoute() {
		//fail("Not yet implemented");
		List<PointInGrid> coordinates = null;
		int cas = 4;
		switch(cas) {
		case 1:
			coordinates = logic.pointsOnRoute(new PointInGrid(1000, 1800),
				ConnectionPoints.get(1), 2);
			break;
			
		case 2:
			coordinates = logic.pointsOnRoute(new PointInGrid(100, 1800), 
					ConnectionPoints.get(0), 3);
				break;
				
		case 3:
			coordinates = logic.pointsOnRoute(new PointInGrid(100, 450), 
					ConnectionPoints.get(2), 4);
				break;
				
		case 4:
			coordinates = logic.pointsOnRoute(new PointInGrid(1000, 450), 
					ConnectionPoints.get(3), 1);
				break;
		}
		for (PointInGrid p : coordinates) {
			System.out.println("X: " + p.getX() + "        Y: " + p.getY());
		}
	}
	
	@Test
	public void testEvalRoute() {
		
		PointInGrid point = logic.EvalRoute(logic.getRobotMiddle(), logic.getConnectionPoints().get(1), logic.findNearestBall(logic.getRobotMiddle(), logic.getBalls()));
		System.out.println("\n\n\n90 degree turn to ball from point: " + point.getX() + ", " + point.getY());
		
	}

}
