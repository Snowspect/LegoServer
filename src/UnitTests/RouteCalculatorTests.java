package UnitTests;

import static org.junit.Assert.*;

import java.awt.Point;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
		
		Point controlpoint = new Point(1000, 1200);
		Point destinationPoint = new Point(400, 600);
		Point robotCenterPoint = new Point(800, 1200);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(360-236, angle, 1);
		
	}
	
	@Test
	public void testAngle2() {
		
		Point controlpoint = new Point(1000, 1200);
		Point destinationPoint = new Point(400, 600);
		Point robotCenterPoint = new Point(200, 1000);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(360-282, angle, 1);
	}
	
	@Test
	public void testAngle3() {
		
		Point controlpoint = new Point(400, 800);
		Point destinationPoint = new Point(1000, 400);
		Point robotCenterPoint = new Point(600, 200);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(360-278, angle, 1);
	}

//	@Test
//	public void testAngle4() {
//		
//		Point controlpoint = new Point(600, 400);
//		Point destinationPoint = new Point(1000, 600);
//		Point robotCenterPoint = new Point(800, 400);
//		
//		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
//		
//		assertEquals(360-225, angle, 1);
//	}
//	
//	@Test
//	public void testAngle5() {
//		
//		Point controlpoint = new Point(400, 1800);
//		Point destinationPoint = new Point(1000, 600);
//		Point robotCenterPoint = new Point(800, 1200);
//		
//		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
//		
//		assertEquals(-180+164, angle, 1);
//	}
//	
//	@Test
//	public void testAngle6() {
//		
//		Point controlpoint = new Point(400, 1800);
//		Point destinationPoint = new Point(1000, 600);
//		Point robotCenterPoint = new Point(400, 1000);
//		
//		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
//		
//		assertEquals(360-236, angle, 1);
//	}
	
}
