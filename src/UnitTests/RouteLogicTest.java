package UnitTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Logic.*;

public class RouteLogicTest {

	
	IRouteLogic logic;
	
	@Before
	public void setUp() throws Exception {
		logic = new RouteLogic();
	}

	@After
	public void tearDown() throws Exception {
		logic = null;
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
