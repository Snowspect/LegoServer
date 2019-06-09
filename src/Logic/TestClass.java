package Logic;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import RouteCalculator.PointInGrid;

public class TestClass {
	private static List<PointInGrid> connectionPoints = new ArrayList<PointInGrid>();
	private static List<PointInGrid> listofBallCoords = new ArrayList<PointInGrid>();
	private static PointInGrid robotMiddle = new PointInGrid(0, 0);
	private static PointInGrid robotFront = new PointInGrid(0, 0);
	private static int rows = 20;
	private static int columns = 20;
	private static int[][] SimulatedGrid = new int[20][20];	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		CreateGrid();
		findElementsInGrid();
		
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
		//first for loop iterates downwards through rows
		//second for loop iterates to the right through columns
		for (int a = 0; a <= rows; a++) {
			for (int b = 0; b < columns; b++) {
				if((a == 1 || a == 18) && b >= 1 && b <= 18) SimulatedGrid[a][b] = 1;
				if((b == 1 || b == 18) && a >= 1 && a <= 18) SimulatedGrid[a][b] = 1;
				if(a == 9 && b >= 6 && b <= 13) SimulatedGrid[a][b] = 1;
				if(b == 9 && a >= 6 && a <= 13) SimulatedGrid[a][b] = 1;
			}
		}
		
		/**
		 * This inserts the four connection points into the map
		 */
			for (int a = 0; a < rows; a++) {
				for (int b = 0; b < columns; b++) {
					if((a == 3 && b == 3)) SimulatedGrid[a][b] = 5;
					if((b == 3 && a == 16)) SimulatedGrid[a][b] = 6;
					if((a == 16 && b == 16)) SimulatedGrid[a][b] = 7;
					if((b == 16 && a == 3)) SimulatedGrid[a][b] = 8;
				}
			}
		
		/**
		 * This line inserts the robot into the grid
		 */
		int RobotFront = 3;
		int RobotMid = 2;
		SimulatedGrid[5][4] = RobotMid;
		SimulatedGrid[5][5] = RobotFront;
			
		/**
		 * This inserts 6 balls into the system, whereas one is outside the main barrier.
		 */
		int ball = 4;
		SimulatedGrid[12][5] = ball;
		SimulatedGrid[10][10] = ball;
		SimulatedGrid[5][14] = ball;
		SimulatedGrid[3][6] = ball;
		SimulatedGrid[12][15] = ball;
		SimulatedGrid[19][19] = ball;
	}
}