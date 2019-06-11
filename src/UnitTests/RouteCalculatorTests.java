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

//	@Test
//	public void testAngle1() {
//		
//		PointInGrid controlpoint = new PointInGrid(6, 2);
//		PointInGrid destinationPoint = new PointInGrid(15, 4);
//		PointInGrid robotCenterPoint = new PointInGrid(17, 2);
//		
//		double angle = RouteCalc.calc_Angle(controlpoint, destinationPoint, robotCenterPoint);
//		
//		assertEquals(45, angle, 0);
//		
//	}
	
	@Test
	public void testAngle2() {
		
		PointInGrid controlpoint = new PointInGrid(400, 300);
		PointInGrid destinationPoint = new PointInGrid(100, 200);
		PointInGrid robotCenterPoint = new PointInGrid(400, 200);
//		RouteCalc.calc_Angle(conPoint, posPoint, destPoint)
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertTrue(angle == 90 || angle == -90 || angle == 270 || angle == -270);
		
//		assertEquals(90, angle, 0.5);
	}
	
//	@Test
//	public void testAngle3() {
//		
//		PointInGrid controlpoint = new PointInGrid(11, 7);
//		PointInGrid destinationPoint = new PointInGrid(14, 16);
//		PointInGrid robotCenterPoint = new PointInGrid(9, 7);
//		
//		double angle = RouteCalc.calc_Angle(controlpoint, destinationPoint, robotCenterPoint);
//		
//		assertEquals(-60.5, angle, 0.5);
//	}

}
