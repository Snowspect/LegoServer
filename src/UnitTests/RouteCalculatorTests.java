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
		
		PointInGrid controlpoint = new PointInGrid(800, 800);
		PointInGrid destinationPoint = new PointInGrid(401, 522);
		PointInGrid robotCenterPoint = new PointInGrid(600, 600);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(156, Math.abs(angle), 1);
		
	}
	
	@Test
	public void testAngle2() {
		
		PointInGrid controlpoint = new PointInGrid(1000, 1000);
		PointInGrid destinationPoint = new PointInGrid(400, 1200);
		PointInGrid robotCenterPoint = new PointInGrid(600, 1200);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(206, Math.abs(angle), 1);
	}
	
	@Test
	public void testAngle3() {
		
		PointInGrid controlpoint = new PointInGrid(1000, 1400);
		PointInGrid destinationPoint = new PointInGrid(400, 1200);
		PointInGrid robotCenterPoint = new PointInGrid(600, 1200);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(154, Math.abs(angle), 1);
	}

	@Test
	public void testAngle4() {
		
		PointInGrid controlpoint = new PointInGrid(1000, 1400);
		PointInGrid destinationPoint = new PointInGrid(400, 1200);
		PointInGrid robotCenterPoint = new PointInGrid(1200, 800);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(45, Math.abs(angle), 1);
	}
	
	@Test
	public void testAngle5() {
		
		PointInGrid controlpoint = new PointInGrid(1000, 400);
		PointInGrid destinationPoint = new PointInGrid(600, 800);
		PointInGrid robotCenterPoint = new PointInGrid(1200, 800);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(360-296, Math.abs(angle), 1);
	}
	
	@Test
	public void testAngle6() {
		
		PointInGrid controlpoint = new PointInGrid(1000, 1000);
		PointInGrid destinationPoint = new PointInGrid(400, 400);
		PointInGrid robotCenterPoint = new PointInGrid(1200, 800);
		
		double angle = RouteCalc.calc_Angle(controlpoint, robotCenterPoint, destinationPoint);
		
		assertEquals(71, Math.abs(angle), 1);
	}
	
}
