package UnitTests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Logic.*;
import RouteCalculator.*;

public class RouteLogicTest {

	
	RouteLogic logic;
	
	@Before
	public void setUp() throws Exception {
		logic = new RouteLogic();
		logic.CreateGrid();
		logic.findElementsInGrid();
		
	}

	@After
	public void tearDown() throws Exception {
		logic = null;
	}

	@Test
	public void test() {
		List<PointInGrid> balls = logic.BallsWithDirectPath(logic.getRobotMiddle(), logic.getBalls());
		
		assertEquals(2, balls.size());
		assertEquals(new PointInGrid(3,6), balls.get(0));
		assertEquals(new PointInGrid(12,15), balls.get(1));
		
	}

}
