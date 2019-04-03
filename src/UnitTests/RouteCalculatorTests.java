package UnitTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import RouteCalculator.PointInGrid;
import RouteCalculator.RouteCalculator;

public class RouteCalculatorTests {
	
	RouteCalculator RouteCalc;

	@Before
	public void setUp() throws Exception {
		RouteCalc = new RouteCalculator();
	}

	@After
	public void tearDown() throws Exception {
		RouteCalc = null;
	}

	@Test
	public void testAngle1() {
		
		PointInGrid controlpoint = new PointInGrid(6, 2);
		PointInGrid destinationPoint = new PointInGrid(15, 4);
		PointInGrid robotCenterPoint = new PointInGrid(17, 2);
		
		double angle = RouteCalc.calc_Angle(controlpoint.getX(), controlpoint.getY(), destinationPoint.getX(), destinationPoint.getY(), robotCenterPoint.getX(), robotCenterPoint.getY());
		
		assertEquals(45, angle, 0);
		
	}
	
	@Test
	public void testAngle2() {
		
		PointInGrid controlpoint = new PointInGrid(7, 5);
		PointInGrid destinationPoint = new PointInGrid(5, 6);
		PointInGrid robotCenterPoint = new PointInGrid(6, 6);
		
		double angle = RouteCalc.calc_Angle(controlpoint.getX(), controlpoint.getY(), destinationPoint.getX(), destinationPoint.getY(), robotCenterPoint.getX(), robotCenterPoint.getY());
		
		assertEquals(-225, angle, 0.5);
	}
	
	@Test
	public void testAngle3() {
		
		PointInGrid controlpoint = new PointInGrid(11, 7);
		PointInGrid destinationPoint = new PointInGrid(14, 16);
		PointInGrid robotCenterPoint = new PointInGrid(9, 7);
		
		double angle = RouteCalc.calc_Angle(controlpoint.getX(), controlpoint.getY(), destinationPoint.getX(), destinationPoint.getY(), robotCenterPoint.getX(), robotCenterPoint.getY());
		
		assertEquals(-60.5, angle, 0.5);
	}

}
