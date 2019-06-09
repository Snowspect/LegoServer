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
		System.out.println("Testing calculation of angles");
	}

	@After
	public void tearDown() throws Exception {
		RouteCalc = null;
	}

	@Test
	public void testAngle1() {
		
		PointInGrid controlpoint = new PointInGrid(100, 300);
		PointInGrid destinationPoint = new PointInGrid(250, 400);
		PointInGrid robotCenterPoint = new PointInGrid(300, 150);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		System.out.println("ANGLE: " + angle +"\n");
		assertEquals(42, angle, 1);
		
	}
	
	@Test
	public void testAngle2() {
		
		PointInGrid controlpoint = new PointInGrid(400, 100);
		PointInGrid destinationPoint = new PointInGrid(600, 400);
		PointInGrid robotCenterPoint = new PointInGrid(500, 300);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		System.out.println("ANGLE: " + angle +"\n");
		assertEquals(198, angle, 1);
	}
	
	@Test
	public void testAngle3() {
		
		PointInGrid controlpoint = new PointInGrid(500, 100);
		PointInGrid destinationPoint = new PointInGrid(800, 100);
		PointInGrid robotCenterPoint = new PointInGrid(500, 300);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		System.out.println("ANGLE: " + angle +"\n");
		assertEquals(-56, angle, 1);
	}

}
