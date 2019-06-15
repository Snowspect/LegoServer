package RobotControl;
import org.jfree.chart.block.GridArrangement;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.*;
import java.math.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sound.sampled.Line;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.plaf.ColorUIResource;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;

import static org.opencv.core.Core.inRange;

public class Billedbehandling
{
    // The camera has a maximum resolution of 1920x1080
    private static int imageWidth = 1920;                		// Image width
    private static int imageHeight = 1080;             			// Image height

    // Color range for detecting BLUE circle on robot
    private static Scalar minBlue = new Scalar(110, 0, 0, 0);  	// BGR-A (NOT RGB!)
    private static Scalar maxBlue = new Scalar(255, 100, 100, 0); // BGR-A (NOT RGB!)

    // Color range for detecting GREEN circle on robot
    //private static Scalar minGreen = new Scalar(0, 100, 0, 0);	// BGR-A (NOT RGB!)
    //private static Scalar maxGreen = new Scalar(90, 255, 90, 0);// BGR-A (NOT RGB!)
    private static Scalar minGreen = new Scalar(0, 110, 0, 0);	// BGR-A (NOT RGB!)
    private static Scalar maxGreen = new Scalar(100, 255, 100, 0);// BGR-A (NOT RGB!)

    // Color range for dynamic GREEN & BLUE
    private static Scalar minBlueDynamic = new Scalar(90, 0, 0, 0);  	// BGR-A (NOT RGB!)
    private static Scalar maxBlueDynamic = new Scalar(255, 70, 65, 0); // BGR-A (NOT RGB!)
    private static Scalar minGreenDynamic = new Scalar(0, 100, 0, 0);	// BGR-A (NOT RGB!)
    private static Scalar maxGreenDynamic = new Scalar(90, 255, 90, 0);// BGR-A (NOT RGB!)

	// Variables to be fetched by the logic
    public static int[][] arrayMap = new int[imageHeight][imageWidth];
    public static List<Point> squareCorners = new ArrayList<>();
    public static List<Point> listOfBallCoordinates = new ArrayList<>();
    public static Point robotBlueMarker, robotGreenMarker;

    // Creating an array of points
    private static Point[] robotCameraPoints;

    // Default file, change this to your own path
    private static String default_file = "C:\\Users\\Niklas\\Desktop\\test_dots.png";

    private static Mat orgMatrix, modMatrix;

    private static JLabel label1, label2, label3, label4;

    private VideoCapture capture;
    
    // Corners
	private static int[] venstreTop = {448, 184};
	private static int[] venstreBund = {440, 911};
	private static int[] hoejreBund = {1440, 906};
	private static int[] hoejreTop = {1435, 181};
    
	private static int[] iVT = {464, 198};
	private static int[] iVB= {456, 900};
	private static int[] iHB = {1423, 894};
	private static int[] iHT = {1419, 195};
	
    // Instantiating the imgcodecs class
    static Imgcodecs imageCodecs = new Imgcodecs();

    // Compulsory
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	// Niklas #####################################
    static Mat src = new Mat();
    static Mat srcGray = new Mat();
    static JFrame frame;
    static JPanel panel = new JPanel(new GridLayout(0, 1));
    static JLabel imgLabel;
    static final int MAX_THRESHOLD = 100;
    static int maxCorners = 36;
    static Random rng = new Random(12345);
    public static boolean camReady = false;
    // ############################################

    public Billedbehandling()
    {
        // Initializing video capture | the image needs to be in a 1920x1080 form factor
    	capture = new VideoCapture(1);
    	
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, imageWidth);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, imageHeight);

        openDebugGUI();
        
    } // End of main()

	public void runImageRec()
	{
		robotCameraPoints = new Point[2];
        robotBlueMarker = new Point();
        robotGreenMarker = new Point();
        orgMatrix = new Mat();
        modMatrix = new Mat();

        // Saving the input from the camera capture to the new matrix
		capture.read(orgMatrix);
		
		modMatrix = orgMatrix.clone();
		
    	/*
    	// Load an image
        orgMatrix = Imgcodecs.imread(default_file, Imgcodecs.IMREAD_COLOR);
        // Check if image is loaded correctly
        if (orgMatrix.empty()) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default " + default_file + "] \n");
            System.exit(-1);
        }
        */

        // Running color detection
        Mat isolatedRedColor = new Mat();
        isolatedRedColor = runColorDetection(orgMatrix);

        // Edge detection
        Mat isolatedEdges = new Mat();
        isolatedEdges = runEdgeDetection(isolatedRedColor);
        
        // Dynamic corner detection
        squareCorners = dynamicCornerDetection(isolatedRedColor);
        
        // Neutralization of the corner points
        squareCorners.set(0, calculateActualCoordinates(squareCorners.get(0), "edge"));
        squareCorners.set(1, calculateActualCoordinates(squareCorners.get(1), "edge"));
        squareCorners.set(2, calculateActualCoordinates(squareCorners.get(2), "edge"));
        squareCorners.set(3, calculateActualCoordinates(squareCorners.get(3), "edge"));
        
        // Create a new outline for the obstacle course
        printOutlineToOrigImg(squareCorners);
        
        // Running ball detection method.
        arrayMap = findBalls(orgMatrix, isolatedRedColor, arrayMap);
        
        // Delete balls outside of square | Do this before calibration
        evaluateBallLocation();
        
        // Calculating "actual" coordinates for each ball.
        for (int i = 0; i < listOfBallCoordinates.size(); i++) {
        	Point temp = new Point();
        	temp = calculateActualCoordinates(listOfBallCoordinates.get(i), "ball");
        	listOfBallCoordinates.set(i, temp);
		}
		

        // Estimating Robot Coordinates based on image from webcam
        robotCameraPoints = newRobotDetect(orgMatrix);

        // Calculating the actual coordinates of the first robot marker
        robotBlueMarker = calculateActualCoordinates(robotCameraPoints[0], "robot");
        robotGreenMarker = calculateActualCoordinates(robotCameraPoints[1], "robot");

        doFrameReprint(orgMatrix, modMatrix, isolatedRedColor, isolatedEdges);
  	}

	private static List<Point> dynamicCornerDetection(Mat src) 
	{			
		Point dVT = new Point(600,300);
		Point dVB = new Point(600,700);
		Point dHB = new Point(1300,300);
		Point dHT = new Point(1300,700);
		
		double tempX = 0;
		double tempY = 0;
		
        BufferedImage buffImg = null;
    	try {
			buffImg = Mat2BufferedImage(src);
		} catch (IOException e) {
			e.printStackTrace();
		}

    	// TOP LEFT CORNER ----------------------------------------
    	for (int x = (int)dVT.x; x > 0; x--) {
    		Color imgColor = new Color(buffImg.getRGB(x, (int)dVT.y));
    		if((imgColor.getRed() > 200) && (imgColor.getGreen() > 200) && (imgColor.getGreen() > 200)) {
    			tempX = x;
    			break;
    		} else tempX = dVT.x;
    	}
    	for (int y = (int)dVT.y; y > 0; y--) {
    		Color imgColor = new Color(buffImg.getRGB((int)dVT.x, y));
    		if((imgColor.getRed() > 200) && (imgColor.getGreen() > 200) && (imgColor.getGreen() > 200)) {
    			tempY = y;
    			break;
    		} else tempY = (int)dVT.y;
		}
    	dVT = new Point(tempX, tempY);
    	
    	// LOWER LEFT CORNER ----------------------------------------
    	for (int x = (int)dVB.x; x > 0; x--) {
    		Color imgColor = new Color(buffImg.getRGB(x, (int)dVB.y));
    		if((imgColor.getRed() > 200) && (imgColor.getGreen() > 200) && (imgColor.getGreen() > 200)) {
    			tempX = x;
    			break;
    		} else tempX = dVB.x;
    	}
    	for (int y = (int)dVB.y; y < 1080; y++) {
    		Color imgColor = new Color(buffImg.getRGB((int)dVB.x, y));
    		if((imgColor.getRed() > 200) && (imgColor.getGreen() > 200) && (imgColor.getGreen() > 200)) {
    			tempY = y;
    			break;
    		} else tempY = (int)dVB.y;
		}
    	dVB = new Point(tempX, tempY);
    	
    	
    	// LOWER RIGHT CORNER ----------------------------------------
    	for (int x = (int)dHB.x; x < 1920; x++) {
    		Color imgColor = new Color(buffImg.getRGB(x, (int)dHB.y));
    		if((imgColor.getRed() > 200) && (imgColor.getGreen() > 200) && (imgColor.getGreen() > 200)) {
    			tempX = x;
    			break;
    		} else tempX = dHB.x;
    	}
    	for (int y = (int)dHB.y; y < 1080; y++) {
    		Color imgColor = new Color(buffImg.getRGB((int)dHB.x, y));
    		if((imgColor.getRed() > 200) && (imgColor.getGreen() > 200) && (imgColor.getGreen() > 200)) {
    			tempY = y;
    			break;
    		} else tempY = (int)dHB.y;
		}
    	dHB = new Point(tempX, tempY);
    	
    	// TOP LEFT CORNER ----------------------------------------
    	for (int x = (int)dHT.x; x < 1920; x++) {
    		Color imgColor = new Color(buffImg.getRGB(x, (int)dHT.y));
    		if((imgColor.getRed() > 200) && (imgColor.getGreen() > 200) && (imgColor.getGreen() > 200)) {
    			tempX = x;
    			break;
    		} else tempX = dHT.x;
    	}
    	for (int y = (int)dHT.y; y > 0; y--) {
    		Color imgColor = new Color(buffImg.getRGB((int)dHT.x, y));
    		if((imgColor.getRed() > 200) && (imgColor.getGreen() > 200) && (imgColor.getGreen() > 200)) {
    			tempY = y;
    			break;
    		} else tempY = (int)dHT.y;
		}
    	dHT = new Point(tempX, tempY);        
        
        // Printing all detected corners:
        Imgproc.circle(modMatrix, dVT, 1, new Scalar(0, 128, 255), 3, 0, 0);
        Imgproc.circle(modMatrix, dVB, 1, new Scalar(0, 128, 255), 3, 0, 0);
        Imgproc.circle(modMatrix, dHB, 1, new Scalar(0, 128, 255), 3, 0, 0);
        Imgproc.circle(modMatrix, dHT, 1, new Scalar(0, 128, 255), 3, 0, 0);
        
		List<Point> CornersList = new ArrayList();
	    CornersList.add(dVT);
	    CornersList.add(dVB);
	    CornersList.add(dHT);
	    CornersList.add(dHB);
        
		return CornersList;

	} // End of dynamicCornerDetection() 
	
	
	private static Point[] newRobotDetect(Mat localOrgMat)
    {
		Mat src = new Mat();
		src = localOrgMat.clone();

        // Creating new matrix to hold grayscale image information
        Mat gray = new Mat();
        Mat print = new Mat(imageHeight, imageWidth, CvType.CV_8U);

        // Converting the original image (src) into an grayscale image and saving it as grey
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Adding some blur to the image to smooth out edges
        Imgproc.medianBlur(gray, gray, 5);

        // Creating new matrix to hold the detected circles
        Mat circlesRobot = new Mat();

        // Detecting circles from the grayscale image and saving it in the circles matrix
        Imgproc.HoughCircles(gray,
        		circlesRobot,
        		Imgproc.HOUGH_GRADIENT,
        		1.0,
                (double) gray.rows() / 25,  	// change this value to detect circles with different distances to each other (orig: 8)
                10.0,
                30.0,
                13, 							// Minimum radius (16 - old val)
                20);           					// Maximum radius (20 - old val)

        Point greenCircle = new Point();        
        Point blueCircle = new Point();
        int blueMax = 0, greenMax = 0, readColor = 0;
        int R = 0; int G = 0; int B = 0;

        BufferedImage buffImg = null;
    	try {
			buffImg =  Mat2BufferedImage(src);
		} catch (IOException e) {
			e.printStackTrace();
		}

        for (int i = 0; i < circlesRobot.cols(); i++)
        {
            double[] cRobot = circlesRobot.get(0, i);
            // Calculation center of the circle
            Point centerRobot = new Point(Math.round(cRobot[0]), Math.round(cRobot[1]));
            int radius = (int) Math.round(cRobot[2]);

            Color imgColor = new Color(buffImg.getRGB((int)centerRobot.x, (int)centerRobot.y));

            R = imgColor.getRed();
            G = imgColor.getGreen();
            B = imgColor.getBlue();

			if (R < 175 && G > greenMax && B < 175) {
				greenMax = G;
				greenCircle = centerRobot;
			}
			if (R < 175 && G < 175 && B > blueMax) {
				blueMax = B;
				blueCircle = centerRobot;
			}

			Imgproc.circle(modMatrix, centerRobot, 1, new Scalar(255, 255, 255), 3, 0, 0);
            Imgproc.circle(modMatrix, centerRobot, radius, new Scalar(0, 0, 0), 3, 0, 0);

        } // End of for loop for each detected circle

        Point[] finalPoints = new Point[2];
        finalPoints[0] = new Point(blueCircle.x, blueCircle.y);
        finalPoints[1] = new Point(greenCircle.x, greenCircle.y);

        return finalPoints;

    } // End of private static void runOpenCV(...)
	

    /**
     * The method takes as input a center point of the circle on the robot,
     * detected by the camera. The function then returns the actual values of this point.
     * Taken into account, that the height of the robot and angel from the camera
     * can have an effect on the points location on the grid.
     * @param localPoint
     * @param objectType
     * @return point
     */
    private static Point calculateActualCoordinates(Point inputPoint, String objectType)
    {
    	Point localPoint = new Point();
    	localPoint = inputPoint;

    	// Calculating how many pixels it takes to get a mm.
    	double mmToPixel = 2.34; // We have calculated the value to 1,7 at center and 2,34 at the corner

        // Measurements (camera, robot, ball and obstacles)
        double cameraHeight = 2000 / 2; 		// 2000mm = 200cm
        double robotHeight = 290; 				// 255mm = 25.5cm
        double ballHeight = 25;					// 40mm = 4cm
        double courseEdgeHeight = 74;			// 70mm = 7cm
        double crossHeight = 30;				// 30mm = 3cm
        Point imageCenter = new Point(imageWidth/2, imageHeight/2);

    	// Boolean to enable console comments
    	Boolean enableComments = false;

    	// To load the height of the given object
    	double objectHeight = 0;

    	switch (objectType) {
			case "robot":
				objectHeight = robotHeight / mmToPixel;
				break;
			case "ball":
				objectHeight = ballHeight / mmToPixel;
				break;
			case "edge":
				objectHeight = courseEdgeHeight / mmToPixel;
				break;
			case "cross":
				objectHeight = crossHeight / mmToPixel;
			default:
				objectHeight = robotHeight / mmToPixel;
				System.out.println("No object type recognized");
				break;
		}

    	// Calculating the distance between a CirclePoint and the center of the image.
    	double pointToCenterDistance = Point2D.distance(imageCenter.x, imageCenter.y, localPoint.x, localPoint.y);
    	if(enableComments) System.out.println("Distance between circle center and image center : " + pointToCenterDistance);

    	// Calculating the direct distance between webcam and CirclePoint (Hypotenuse).
    	double pointToWebcamDistance = Math.hypot(cameraHeight, pointToCenterDistance);
    	if(enableComments)System.out.println("Distance between circle center and webcam : " + pointToWebcamDistance);

    	// Calculating the angle between CirclePoint and webcam.
    	double pointToWebcamAngle = Math.asin(cameraHeight / pointToWebcamDistance);
    	if(enableComments)System.out.println("Angle between circle center and webcam : " + (Math.toDegrees(pointToWebcamAngle)));

    	// Calculating the distance between CirclePoint and actual RobotPoint.
    	double differenceBetweenPointAndActualValue = objectHeight * Math.tan(Math.toRadians(90 - Math.toDegrees(pointToWebcamAngle)));
    	if(enableComments)System.out.println("Distance to corrected : " + differenceBetweenPointAndActualValue);

    	// Version 1 | Updating the RobotPoint value so that the difference is added.
    	double distanceRatio = differenceBetweenPointAndActualValue / pointToCenterDistance;
    	Point pointToBeReturned = new Point(
    			((1-distanceRatio)*localPoint.x + distanceRatio*imageCenter.x) ,
    			((1-distanceRatio)*localPoint.y + distanceRatio*imageCenter.y) );

    	if(enableComments) {
	    	System.out.println(" -------------------------------------- ");
	    	System.out.println("Center coordinat : x = " + (int)imageCenter.x + " , y = " + (int)imageCenter.y);
	    	System.out.println("Orig coordinates : x = " + localPoint.x + " , y = " + localPoint.y);
	    	System.out.println("Robot coordinate : x = " + pointToBeReturned.x + " , y = " + pointToBeReturned.y);
	    	System.out.println(" -------------------------------------- ");
	    	System.out.println(" -------------------------------------- ");
    	}

    	if(objectType.equals("robot")) {
			Imgproc.circle(modMatrix,       // Circle center
	                pointToBeReturned,
	                10,
	                new Scalar(0, 0, 0),
	                2,
	                0,
	                0);
	
			Imgproc.circle(modMatrix,       // Circle center
	                pointToBeReturned,
	                1,
	                new Scalar(0, 0, 0),
	                3,
	                0,
	                0);
    	} else if(objectType.equals("ball")) {
			Imgproc.circle(modMatrix,       // Circle center
	                pointToBeReturned,
	                10,
	                new Scalar(0, 128, 255),
	                2,
	                0,
	                0);
	
			Imgproc.circle(modMatrix,       // Circle center
	                pointToBeReturned,
	                1,
	                new Scalar(0, 128, 255),
	                3,
	                0,
	                0);
    	}

    	// Returning the calculated robot coordinate
    	return pointToBeReturned;
    	
    } // End of robotCalculateCoordinates()

    
    /**
     *
     * @param localPoints
     */
    private static void printOutlineToOrigImg(List<Point> localPoints)
    {
        int VT = 0;		int VB = 1;		int HT = 2;		int HB = 3;
        
        Imgproc.line(modMatrix, localPoints.get(VT), localPoints.get(HT), new Scalar(200, 200, 0, 255), 2);
        Imgproc.line(modMatrix, localPoints.get(HT), localPoints.get(HB), new Scalar(200, 200, 0, 255), 2);
        Imgproc.line(modMatrix, localPoints.get(HB), localPoints.get(VB), new Scalar(200, 200, 0, 255), 2);
        Imgproc.line(modMatrix, localPoints.get(VB), localPoints.get(VT), new Scalar(200, 200, 0, 255), 2);
    }
    

    
    /**
     * Takes filename of picture as input.
     * Calculates the average distance between three balls.
     * @param filename
     */
    private static void findPixelSize(String filename)
    {
        // Load an image
        Mat src = Imgcodecs.imread(filename, Imgcodecs.IMREAD_COLOR);

        // Check if image is loaded correctly
        if (src.empty()) {
            System.out.println("Error opening image!");
            System.exit(-1);
        }

        // Creating new matrix to hold grayscale image information and one for detected circles
        Mat gray = new Mat();
        Mat circles = new Mat();

        // Converting the original image (src) into an grayscale image and saving it as grey
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Adding some blur to the image to smooth out edges
        Imgproc.medianBlur(gray, gray, 5);

        // Detecting circles from the grayscale image and saving it in the circles matrix
        Imgproc.HoughCircles(gray,
        		circles,
        		Imgproc.HOUGH_GRADIENT,
        		1.0,
                (double) gray.rows() / 500,  	// change this value to detect circles with different distances to each other (orig: 8)
                25.0,
                14.0,
                9, 								// Minimum radius
                11);           					// Maximum radius
        										// change the last two parameters (orig: 1 , 10)
        										// Latest calibration : 8, 15)
        										// Eclipse calibration : 9, 11)

        Point[] holdCenterPoints = new Point[100];
        for (int x = 0; x < circles.cols(); x++)
        {
            double[] c = circles.get(0, x);
            holdCenterPoints[x] = new Point(c[0], c[1]);
            // Calculation center of the circle
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            System.out.println("Ball center x = " +holdCenterPoints[x].x+ " y = " +holdCenterPoints[x].y);
        } // End of for loop for each detected circle


        System.out.println(Point2D.distance(holdCenterPoints[0].x, holdCenterPoints[0].y, holdCenterPoints[1].x, holdCenterPoints[1].y));
        System.out.println(Point2D.distance(holdCenterPoints[1].x, holdCenterPoints[1].y, holdCenterPoints[2].x, holdCenterPoints[2].y));
        System.out.println(Point2D.distance(holdCenterPoints[2].x, holdCenterPoints[2].y, holdCenterPoints[0].x, holdCenterPoints[0].y));

        double distanceSum = (Point2D.distance(holdCenterPoints[0].x, holdCenterPoints[0].y, holdCenterPoints[1].x, holdCenterPoints[1].y) +
        		Point2D.distance(holdCenterPoints[1].x, holdCenterPoints[1].y, holdCenterPoints[2].x, holdCenterPoints[2].y) +
        		Point2D.distance(holdCenterPoints[2].x, holdCenterPoints[2].y, holdCenterPoints[0].x, holdCenterPoints[0].y));
        double averageDiameter = distanceSum / 3;
        System.out.println("Diameter average : " +averageDiameter+ "pixels");

        double pixel_mm_converter = 40 / averageDiameter;
        System.out.println("One pixel is " +pixel_mm_converter+ "mm");

    } // End of findPixelSize()

    /**
     * The function runOpenCV takes a file for the original image.
     * A file that specifies where to save the new image and a matrix
     * containing the output from the color detection function.
     * @param filename
     * @param default_file
     * @param isolatedRedColor
     */
    private static int[][] findBalls(Mat localOrgMatrix, Mat isolatedRedColor, int[][] localMap)
    {
    	Mat src = new Mat();
    	src = localOrgMatrix.clone();

    	/*
        // Load an image
        Mat src = Imgcodecs.imread(default_file, Imgcodecs.IMREAD_COLOR);

        // Check if image is loaded correctly
        if (src.empty()) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default " + default_file + "] \n");
            System.exit(-1);
        }
        */

        // Creating new matrix to hold grayscale image information
        Mat gray = new Mat();

        // Converting the original image (src) into an grayscale image and saving it as grey
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Adding some blur to the image to smooth out edges
        Imgproc.medianBlur(gray, gray, 5);

        // Creating new matrix to hold the detected circles
        Mat circles = new Mat();

        // Clearing the list of ball center coordiantes
        listOfBallCoordinates.clear();

        // Detecting circles from the grayscale image and saving it in the circles matrix
        Imgproc.HoughCircles(gray,
        		circles,
        		Imgproc.HOUGH_GRADIENT,
        		1.0,
                (double) gray.rows() / 25,  	// change this value to detect circles with different distances to each other (orig: 8)
                25.0,
                14.0,
                9, 								// Minimum radius
                11);           					// Maximum radius
        										// change the last two parameters (orig: 1 , 10)
        										// Latest calibration : 8, 15)
        										// Eclipse calibration : 9, 11)

        listOfBallCoordinates.clear();
        for (int x = 0; x < circles.cols(); x++)
        {
            double[] c = circles.get(0, x);

            // Calculation center of the circle
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));

            // Calculation radius of the circle
            double radius = Math.round(c[2]);

            // Parsing a double value to an integer
            arrayMap[(int)center.y][(int)center.x] = 2;

            // Adding ball coordinates to the list of ball center coordinates
            //System.out.println("Coordinates : x = " +center.x+ " , y = " +center.y);
            listOfBallCoordinates.add(center);

            // ---------------------------------------------------------------------------------------------------------
            // Used to write information to the image already containing information about the colored barriers
            Imgproc.circle(isolatedRedColor,       	// Circle center
                    center,
                    1,
                    new Scalar(255, 255, 255),
                    0,
                    0,
                    0);

            Imgproc.circle(isolatedRedColor,      	// Circle outline
                    center,
                    (int)radius,
                    new Scalar(255, 255, 255),
                    1,
                    8,
                    0);

            Imgproc.circle(modMatrix,       		// Circle center
                    center,
                    1,
                    new Scalar(0, 0, 0),
                    2,
                    0,
                    0);

            Imgproc.circle(modMatrix,      			// Circle outline
                    center,
                    (int)radius,
                    new Scalar(0, 0, 0),
                    2,
                    8,
                    0);

            // Saving the image path and writing the new image
            //String file = "C:\\Users\\Bruger\\Desktop\\Legobot\\test1.png";
            //imageCodecs.imwrite(file, isolatedRedColor);
            // ---------------------------------------------------------------------------------------------------------
        } // End of for loop for each detected circle

        return localMap;

    } // End of private static void runOpenCV(...)


	private static void evaluateBallLocation() 
	{
		Point VT = new Point(squareCorners.get(0).x,squareCorners.get(0).y);
		Point VB = new Point(squareCorners.get(1).x,squareCorners.get(1).y);
		Point HT = new Point(squareCorners.get(2).x,squareCorners.get(2).y);
		Point HB = new Point(squareCorners.get(3).x,squareCorners.get(3).y);
			
		//Rect rVTHB = new Rect((int)dVT.x, (int)dVT.y, ((int)dHT.x - (int)dVT.x), ((int)dVB.y - (int)dVT.y));
		Rect rVTHB = new Rect(HB, VT);
				
		List<Point> validBalls = new ArrayList<>();	
		
		for (Point p : listOfBallCoordinates) {
			if(rVTHB.contains(p)) {
				validBalls.add(p);
			}
		}
		
		listOfBallCoordinates.clear();
		listOfBallCoordinates.addAll(validBalls);
		
	} // End of evaluateBallLocation()
    
    /**
     * Takes a frame as input and returns an matrix with only the red color highlighted
     * @param frame
     */
    private static Mat runColorDetection(Mat localOrgMatrix)
    {
    	Mat frame = new Mat();
    	frame = localOrgMatrix.clone();
    	
    	// Color range for detecting RED
        Scalar min = new Scalar(0, 0, 150, 0);      	// BGR-A (NOT RGB!) (Better than original : (0, 0, 130, 0))
        Scalar max = new Scalar(80, 100, 255, 0);  	// BGR-A (NOT RGB!) (Better than original : (140, 110, 255, 0))
        
        // Initializing color range
        inRange(frame, min, max, frame);
        
        // Adding some blur to the image, so that "mistakes" is washed out
        Imgproc.blur(frame, frame, new Size(3,3), new Point(-1,-1));
        
        //Imgproc.equalizeHist(frame, frame);
        
        // Saving the image path and writing the new image
        //String file = "C:\\Users\\Bruger\\Desktop\\Legobot\\test1.png";
        //imageCodecs.imwrite(file, localOrgMatrix);
        
        return frame;
    }


    /**
     * Takes the image with the color detected edges and
     * isolates the edges of the red parts of the track.
     * @param frame
     * @param file
     */
    private static Mat runEdgeDetection(Mat localFrame)
    {
    	Mat frame = new Mat();
    	frame = localFrame.clone();

        int threshold1 = 10;
        int threshold2 = 150;
        int apertureSize = 3;
        boolean L2gradient = false;

        //Imgproc.blur(temp_mat, gray, new Size(3,3));
        Mat wide = new Mat();
        Imgproc.Canny(frame, wide, threshold1, threshold2, apertureSize, L2gradient);
        
        Mat draw = new Mat();
        wide.convertTo(draw, CvType.CV_8U);
        
        /*
        // Standard Hough Line Transform
        Mat lines = new Mat(); // will hold the results of the detection
        Imgproc.HoughLines(wide, lines, 1, Math.PI/180, 100); // runs the actual detection
        
        // Draw the lines
        for (int x = 0; x < lines.rows(); x++) {
            double rho = lines.get(x, 0)[0],
                    theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a*rho, y0 = b*rho;
            Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
            Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
            Imgproc.line(draw, pt1, pt2, new Scalar(0, 255, 100), 3, Imgproc.LINE_AA, 0);
        }
        
        // Probabilistic Line Transform
        Mat linesP = new Mat(); // will hold the results of the detection
        Imgproc.HoughLinesP(wide, linesP, 1, Math.PI/190, 1, 2, 1000); // runs the actual detection
        
        // Draw the lines
        for (int x = 0; x < linesP.rows(); x++) {
            double[] l = linesP.get(x, 0);
            Imgproc.line(draw, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 128, 255), 3, Imgproc.LINE_AA, 0);
        }
        */
        
        return draw;

        // Saving the calculated matrix to the given path name (file)
        //Imgcodecs.imwrite("C:\\Users\\Bruger\\Desktop\\Legobot\\test_1_edges.png", draw);
    }

    /*
    private static List<Point> detectCorners(Mat localsrc)
    {
    	Mat src = new Mat();
    	src = localsrc.clone();

        maxCorners = Math.max(maxCorners, 1);
        MatOfPoint corners = new MatOfPoint();
        double qualityLevel = 0.01;
        double minDistance = 10;
        int blockSize = 3, gradientSize = 3;
        boolean useHarrisDetector = false;
        double k = 0.04;
        Mat copy = orgMatrix.clone();

        Imgproc.goodFeaturesToTrack(src, corners, maxCorners, qualityLevel, minDistance, new Mat(),
                blockSize, gradientSize, useHarrisDetector, k);
        //System.out.println("** Number of corners detected: " + corners.rows());
        int[] cornersData = new int[(int) (corners.total() * corners.channels())];
        corners.get(0, 0, cornersData);
        int radius = 4;

        double distance_vt = 5000;
        double distance_vb = 5000;
        double distance_ht = 5000;
        double distance_hb = 5000;

        Point distancepoint_vt = null;
        Point distancepoint_vb = null;
        Point distancepoint_ht = null;
        Point distancepoint_hb = null;

        int[] venstreTop = {525, 230};
        int[] venstreBund = {525, 830};
        int[] hoejreBund = {1375, 830};
        int[] hoejreTop = {1375, 230};

        List<Point> PointList = new ArrayList<>();

        for (int i = 0; i < corners.rows(); i++) {
        	
        	 Imgproc.circle(modMatrix, new Point(cornersData[i * 2], cornersData[i * 2 + 1]), 3, new Scalar(215, 120, 0), Core.FILLED);
        	 
        	
            // Adding points to the list
            PointList.add(distancepoint_vt);
            PointList.add(distancepoint_vb);
            PointList.add(distancepoint_ht);
            PointList.add(distancepoint_hb);

            double temp_vt = Math.hypot( venstreTop[0]-cornersData[i * 2], 	venstreTop[1]-cornersData[i * 2 + 1]);
            double temp_vb = Math.hypot( venstreBund[0]-cornersData[i * 2], venstreBund[1]-cornersData[i * 2 + 1]);
            double temp_ht = Math.hypot( hoejreBund[0]-cornersData[i * 2], 	hoejreBund[1]-cornersData[i * 2 + 1]);
            double temp_hb = Math.hypot( hoejreTop[0]-cornersData[i * 2], 	hoejreTop[1]-cornersData[i * 2 + 1]);

            Imgproc.circle(copy, new Point(cornersData[i * 2],cornersData[i * 2 + 1]), 3, new Scalar(215, 120, 0), Core.FILLED);

            if (checkDistance(temp_vt, distance_vt))
            {
                distance_vt = temp_vt;
                distancepoint_vt = new Point(cornersData[i * 2], cornersData[i * 2 + 1]);
                PointList.set(0, distancepoint_vt);
            }

            if (checkDistance(temp_vb, distance_vb))
            {
                distance_vb = temp_vb;
                distancepoint_vb = new Point(cornersData[i * 2], cornersData[i * 2 + 1]);
                PointList.set(1, distancepoint_vb);
            }

            if (checkDistance(temp_ht, distance_ht))
            {
                distance_ht = temp_ht;
                distancepoint_ht = new Point(cornersData[i * 2], cornersData[i * 2 + 1]);
                PointList.set(2, distancepoint_ht);
            }

            if (checkDistance(temp_hb, distance_hb))
            {
                distance_hb = temp_hb;
                distancepoint_hb = new Point(cornersData[i * 2], cornersData[i * 2 + 1]);
                PointList.set(3, distancepoint_hb);
            }
        }

        Imgproc.circle(copy, new Point(venstreTop[0],venstreTop[1]), 10, new Scalar(0, 128, 255), Core.FILLED);
        Imgproc.circle(copy, new Point(venstreBund[0],venstreBund[1]), 10, new Scalar(0, 128, 255), Core.FILLED);
        Imgproc.circle(copy, new Point(hoejreBund[0],hoejreBund[1]), 10, new Scalar(0, 128, 255), Core.FILLED);
        Imgproc.circle(copy, new Point(hoejreTop[0],hoejreTop[1]), 10, new Scalar(0, 128, 255), Core.FILLED);

        Imgproc.circle(modMatrix, distancepoint_vt, radius,
                new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);
        Imgproc.circle(modMatrix, distancepoint_ht, radius,
                new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);
        Imgproc.circle(modMatrix, distancepoint_vb, radius,
                new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);
        Imgproc.circle(modMatrix, distancepoint_hb, radius,
                new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);

        //imgLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(copy)));

        System.out.println("distancepoint_vt:");
        System.out.println("x: " + distancepoint_vt.x + " y:" + distancepoint_vt.y);


        System.out.println("distancepoint_vb:");
        System.out.println("x: " + distancepoint_vb.x + " y:" + distancepoint_vb.y);


        System.out.println("distancepoint_ht:");
        System.out.println("x: " + distancepoint_ht.x + " y:" + distancepoint_ht.y);


        System.out.println("distancepoint_hb:");
        System.out.println("x: " + distancepoint_hb.x + " y:" + distancepoint_hb.y);
        
        
        return PointList;
    }
    */


    private static boolean checkDistance(double temp_vt, double distance_vt)
    {
        if (temp_vt < distance_vt) {
            return true;
        } 
        return false;
    }


    private static void addComponentsToPane(Container pane, Image img)
    {
        if (!(pane.getLayout() instanceof BorderLayout)) {
            pane.add(new JLabel("Container doesn't use BorderLayout!"));
            return;
        }
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        sliderPanel.add(new JLabel("Max  corners:"));
        JSlider slider = new JSlider(0, MAX_THRESHOLD, maxCorners);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        pane.add(sliderPanel, BorderLayout.PAGE_START);
        imgLabel = new JLabel(new ImageIcon(img));
        pane.add(imgLabel, BorderLayout.CENTER);
    }

	private List<Point> detectCorners() 
	{
		/*
		Point VT = new Point(venstreTop[0],venstreTop[1]);
		Point VB = new Point(venstreBund[0],venstreBund[1]);
		Point HT = new Point(hoejreBund[0],hoejreBund[1]);
		Point HB = new Point(hoejreTop[0],hoejreTop[1]);
		*/
		
		Point VT = new Point(iVT[0],iVT[1]);
		Point VB = new Point(iVB[0],iVB[1]);
		Point HT = new Point(iHB[0],iHB[1]);
		Point HB = new Point(iHT[0],iHT[1]);
		
        Imgproc.circle(modMatrix, VT, 2, new Scalar(0, 128, 255), Core.FILLED);
        Imgproc.circle(modMatrix, VB, 2, new Scalar(0, 128, 255), Core.FILLED);
        Imgproc.circle(modMatrix, HT, 2, new Scalar(0, 128, 255), Core.FILLED);
        Imgproc.circle(modMatrix, HB, 2, new Scalar(0, 128, 255), Core.FILLED);
	    
		List<Point> CornersList = new ArrayList();
	    CornersList.add(VT);
	    CornersList.add(VB);
	    CornersList.add(HT);
	    CornersList.add(HB);
	    
		return CornersList;
	} // End of detectCorners() 

    /*
    private void calibrateColor()
    {
    	BufferedImage buffImg = null;

    	try {
			buffImg =  Mat2BufferedImage(matrix);
		} catch (IOException e) {
			e.printStackTrace();
		}

        int VT = 0;		int VB = 1;		int HB = 2;		int HT = 3;


        // Point pVT = null;
        // Point pVB = null;
        // Point pHT = null;
        // Point pHB = null;


        Point pVT = new Point(squareCorners.get(VT).x - 37, squareCorners.get(VT).y - 37);
        Point pVB = new Point(squareCorners.get(VB).x - 37, squareCorners.get(VB).y + 37);
        //Point pHT = new Point(squareCorners.get(HT).x + 20, squareCorners.get(HT).y - 20);
        //Point pHB = new Point(squareCorners.get(HB).x + 20, squareCorners.get(HB).y + 20);

        int[][] pixel = new int[1080][1920];
    	int sumR = 0, 	sumG = 0, 	sumB = 0;

    	for (int x = (int)pVT.x-5; x < (int)pVT.x+5; x++)
    	{
    		for (int y = (int)pVT.y-5; y < (int)pVT.y+5; y++)
    		{
    			pixel[y][x] = (buffImg.getRGB((int)pVT.x, (int)pVT.y));
    			sumR = sumR + (pixel[y][x] >> 16) & 0xff;
    			sumG = sumG + (pixel[y][x] >> 8) & 0xff;
    			sumB = sumB + (pixel[y][x]) & 0xff;
    		}
    	}

        minBlueDynamic = new Scalar((sumB/25)-50, (sumB/25)-20, (sumR/25)-20, 0);  	// BGR-A (NOT RGB!)
        maxBlueDynamic = new Scalar(255, (sumG/25), (sumR/25), 0); // BGR-A (NOT RGB!)

        sumR = 0; 	sumG = 0; 	sumB = 0;

    	for (int x = (int)pVB.x-5; x < (int)pVB.x+5; x++)
    	{
    		for (int y = (int)pVB.y-5; y < (int)pVB.y+5; y++)
    		{
    			pixel[y][x] = (buffImg.getRGB((int)pVB.x, (int)pVB.y));
    			sumR = sumR + (pixel[y][x] >> 16) & 0xff;
    			sumG = sumG + (pixel[y][x] >> 8) & 0xff;
    			sumB = sumB + (pixel[y][x]) & 0xff;
    		}
    	}

        minGreenDynamic = new Scalar((sumB/25)-20, (sumG/25)-50, (sumR/25)-20, 0);  	// BGR-A (NOT RGB!)
        maxGreenDynamic = new Scalar((sumB/25)+20, 255, (sumR/25)+20, 0); // BGR-A (NOT RGB!)

        System.out.println(minBlueDynamic.toString());
        System.out.println(maxBlueDynamic.toString());

        Mat cloneMat = matrix.clone();

        Imgproc.circle(cloneMat,            		// Circle center
                pVT,
                3,
                new Scalar(255, 255, 255),
                0,
                0,
                0);
        Imgproc.circle(cloneMat,            		// Circle center
                pVB,
                3,
                new Scalar(255, 255, 255),
                0,
                0,
                0);

        imageCodecs.imwrite("C:\\Users\\Bruger\\Desktop\\Legobot\\test_dynamic_color.png", cloneMat);
    }
	*/
    
    public static BufferedImage Mat2BufferedImage(Mat matrix)throws IOException {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

	public void doFrameReprint(Mat orgMatrix2, Mat modMatrix2, Mat isolatedRedColor2, Mat isolatedEdges2) 
	{
		label1.setIcon(new ImageIcon(new ImageIcon(HighGui.toBufferedImage(modMatrix2)).getImage().getScaledInstance(label1.getWidth(), label1.getHeight(), Image.SCALE_DEFAULT)));
		//label2.setIcon(new ImageIcon(new ImageIcon(HighGui.toBufferedImage(isolatedEdges2)).getImage().getScaledInstance(label2.getWidth(), label2.getHeight(), Image.SCALE_DEFAULT)));
		//label4.setIcon(new ImageIcon(new ImageIcon(HighGui.toBufferedImage(isolatedRedColor2)).getImage().getScaledInstance(label4.getWidth(), label4.getHeight(), Image.SCALE_DEFAULT)));
		frame.repaint();
	}

	public void openDebugGUI() 
	{
        // Create and set up the window.
        frame = new JFrame("Gruppe 10 - Debug GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1920, 1080));
        frame.setContentPane(panel);
        frame.setVisible(true);
        
        label1 = new JLabel("1");
        panel.add(label1);
        //label2 = new JLabel("2");
        //panel.add(label2);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}

	private Mat cropOrgMatrix() {
    	Mat orgMatrixClone = new Mat();
    	orgMatrixClone = orgMatrix.clone();

		//Rect rectCrop = new Rect(squareCorners.get(0), squareCorners.get(2));

    	int startX = (int) squareCorners.get(0).x;
    	//System.out.println("StartX: " + startX);
    	int startY = (int) squareCorners.get(0).y;
    	//System.out.println("startY: " + startY);
    	int width = (int) squareCorners.get(3).x - (int) squareCorners.get(0).x;
    	//System.out.println("width: " + width);
    	int height = (int) squareCorners.get(1).y - (int) squareCorners.get(0).y;
    	//System.out.println("height: " + height);

		Rect rectCrop = new Rect(startX,startY,width,height);

		Mat croppedImage = new Mat(orgMatrixClone, rectCrop);
		//Mat croppedImage = orgMatrix.clone();


		/*
        int[] venstreTop = {525, 230};
        int[] venstreBund = {525, 830};
        int[] hoejreBund = {1375, 830};
        int[] hoejreTop = {1375, 230};

        Imgproc.circle(orgMatrixClone, new Point(venstreTop[0],venstreTop[1]), 10, new Scalar(0, 128, 0), Core.FILLED);
        Imgproc.circle(orgMatrixClone, new Point(venstreBund[0],venstreBund[1]), 10, new Scalar(0, 128, 0), Core.FILLED);
        Imgproc.circle(orgMatrixClone, new Point(hoejreBund[0],hoejreBund[1]), 10, new Scalar(0, 128, 0), Core.FILLED);
        Imgproc.circle(orgMatrixClone, new Point(hoejreTop[0],hoejreTop[1]), 10, new Scalar(0, 128, 0), Core.FILLED);

        Imgproc.circle(orgMatrixClone, squareCorners.get(0), 10, new Scalar(0,0,255), Core.FILLED);
        Imgproc.circle(orgMatrixClone, squareCorners.get(1), 10, new Scalar(0,0,255), Core.FILLED);
        Imgproc.circle(orgMatrixClone, squareCorners.get(2), 10, new Scalar(0,0,255), Core.FILLED);
        Imgproc.circle(orgMatrixClone, squareCorners.get(3), 10, new Scalar(0,0,255), Core.FILLED);
        */

    	Imgproc.rectangle(orgMatrixClone, squareCorners.get(0), squareCorners.get(2), new Scalar(0, 255, 0, 255), 3);

    	return orgMatrixClone;
	}

	public List<Point> getCorners() {
		return squareCorners;
	}
	
	// *************************************************
	// METHODS TO BE CALLED BY THE LOGIC 
	// *************************************************

    public int[][] getGrid() {
		return arrayMap;
	}
    
	// *************************************************
	// *************************************************

} // End of public class Billedbehandling
