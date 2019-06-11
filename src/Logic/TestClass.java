package Logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import RouteCalculator.RouteCalculator;
import RouteCalculator.RouteCalculatorInterface;
import RouteCalculator.PointInGrid;

public class TestClass {
	private static List<PointInGrid> connectionPoints = new ArrayList<PointInGrid>();
	private static List<PointInGrid> listofBallCoords = new ArrayList<PointInGrid>();
	private static PointInGrid robotMiddle = new PointInGrid(0, 0);
	private static PointInGrid robotFront = new PointInGrid(0, 0);
	private static int rows = 1080;
	private static int columns = 1920;
	private static int[][] SimulatedGrid = new int[rows][columns];	
	
	private static RouteCalculatorInterface Calculator;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//RobotPerimeter(9, 9, 4);
		CreateGrid();
		findElementsInGrid();
		Calculator = new RouteCalculator();
		
		robotFront = new PointInGrid(1,1);
		robotMiddle = new PointInGrid(1,0);
		
		PointInGrid destPoint = new PointInGrid(15,0);
		
		double angle = Calculator.calc_Angle(robotFront, robotMiddle, destPoint);
		
		System.out.println("angle : " + angle);
		
		//Prints out the array in a proper format
		for (int[] is : SimulatedGrid) {
			System.out.println(Arrays.toString(is));
		}
	}
	
	public static void findElementsInGrid()
	{
		int rows = 20;
		int columns = 20;

		for (int a = 0; a < rows; a++) {
			for (int b = 0; b < columns; b++) {
				if(SimulatedGrid[a][b] == 2) robotMiddle = new PointInGrid(a,b);
				if(SimulatedGrid[a][b] == 3) robotFront = new PointInGrid(a,b);
				if(SimulatedGrid[a][b] == 4) {
					PointInGrid tmpBall = new PointInGrid(a, b);
					listofBallCoords.add(tmpBall);
				}
				if(SimulatedGrid[a][b] == 5 || SimulatedGrid[a][b] == 6 ||
				   SimulatedGrid[a][b] == 7 ||SimulatedGrid[a][b] == 8)
				{
					PointInGrid tmpConnPoint = new PointInGrid(a, b);
					connectionPoints.add(tmpConnPoint);
				}
			}
		}
		System.out.println("robot front : " + robotFront.getX() + " : " + robotFront.getY());
		System.out.println("robot middle : " + robotMiddle.getX() + " : " + robotMiddle.getY());
		for (PointInGrid ball : listofBallCoords) {
			System.out.println("ball : x = " + ball.getX() + " , y = " + ball.getY());
		}
		for (PointInGrid connPoint : connectionPoints) {
			System.out.println("connPoint : x = " + connPoint.getX() + " , y = " + connPoint.getY());
		}
	}
	
	public static void CreateGrid()
	{
		/**
		 * This for loop construction simulates walls in the grid
		 */
		int rows = 1080;
		int columns = 1920;
		/**
		 * This for loop construction simulates walls in the grid
		 */
		//first for loop iterates downwards through rows
		//second for loop iterates to the right through columns
		for (int a = 0; a <= rows; a++) {
			for (int b = 0; b < columns; b++) {
				if((a == 1 || a == 1078) && b >= 1 && b <= 1918) SimulatedGrid[a][b] = 1;
				if((b == 1 || b == 1918) && a >= 1 && a <= 1078) SimulatedGrid[a][b] = 1;
				if(a == 9*54 && b >= 6*96 && b <= 13*96) SimulatedGrid[a][b] = 1;
				if(b == 9*96 && a >= 6*54 && a <= 13*54) SimulatedGrid[a][b] = 1;
			}
		}
		
		/**
		 * This inserts the four connection points into the map
		 */
			for (int a = 0; a < rows; a++) {
				for (int b = 0; b < columns; b++) {
					if((a == 3*54 && b == 3*96)) SimulatedGrid[a][b] = 5;
					if((b == 3*96 && a == 16*54)) SimulatedGrid[a][b] = 6;
					if((a == 16*54 && b == 16*96)) SimulatedGrid[a][b] = 7;
					if((b == 16*96 && a == 3*54)) SimulatedGrid[a][b] = 8;
				}
			}
		
		/**
		 * This line inserts the robot into the grid
		 */
			int RobotFront = 3;
			int RobotMid = 2;
			SimulatedGrid[500][1400] = RobotMid;
			SimulatedGrid[670][1400] = RobotFront;
			
		/**
		 * This inserts 6 balls into the system, whereas one is outside the main barrier.
		 * size: 2cm = 
		 */
			int ball = 4;
			//SimulatedGrid[12][5] = ball;
			SimulatedGrid[1000][1000] = ball;
//			SimulatedGrid[12][16] = ball;
//			SimulatedGrid[3][6] = ball;
//			SimulatedGrid[7][15] = ball;
			SimulatedGrid[1000][1900] = ball;
	}
	
	
	//x is robotmiddle.getX(), y is robotmiddle.getY().
	//r is from dist form between robotMiddle and robotFront.
	public static List<PointInGrid> RobotPerimeter(double RmidX, double RmidY, double robotRadius)
	{
		List<PointInGrid> CirclePoints = new ArrayList<PointInGrid>();
		    
		double PI = 3.1415926535;
	    double i, angle, x1, y1;

		    //iterates through all 360 angles
		    for (i = 0; i < 360; i += 1) {
		        angle = i;
		        //finds coordinates on sin and cos with radius
		        x1 = robotRadius * Math.cos(angle * PI / 180);
		        y1 = robotRadius * Math.sin(angle * PI / 180);
		        //
		        int ElX = (int) (RmidX + x1);
		        int ElY = (int) (RmidY + y1);
		        SimulatedGrid[ElX][ElY] = 1;
		        //setElementColor(color);
		        CirclePoints.add(new PointInGrid(ElX,ElY));
		    }
		    return CirclePoints; 
		}
	
}