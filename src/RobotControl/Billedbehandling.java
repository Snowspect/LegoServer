package RobotControl;
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
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.IOException;

import static org.opencv.core.Core.inRange;

public class Billedbehandling
{
    // The camera has a maximum resolution of 1920x1080
    private static int imageWidth = 1920;                		// Image width
    private static int imageHeight = 1080;             			// Image height

	// Variables to be fetched by the logic
    public static int[][] arrayMap = new int[imageHeight][imageWidth];
    public static List<Point> squareCorners = new ArrayList<>();
    public static List<Point> listOfBallCoordinates = new ArrayList<>();
    public static List<Point> crossPoints = new ArrayList<>();
    public static List<Point> crossPointsList = new ArrayList<>();
    public static List<Point> listOfCircleBalls = new ArrayList<>();
    public static List<Point> listOfCircleBallsPickup = new ArrayList<>();
    public static Point robotBlueMarker, robotGreenMarker;

    // Creating an array of points
    private static Point[] robotCameraPoints;

    private static Mat orgMatrix, modMatrix;

    private static JLabel label1;

    private VideoCapture capture;

    /*
    // Corners
	private static int[] venstreTop = {448, 184};
	private static int[] venstreBund = {440, 911};
	private static int[] hoejreBund = {1440, 906};
	private static int[] hoejreTop = {1435, 181};

	private static int[] iVT = {464, 198};
	private static int[] iVB= {456, 900};
	private static int[] iHB = {1423, 894};
	private static int[] iHT = {1419, 195};
	*/

    // Detect Cross boolean, makes sure that the cross detect isnts ran each time
    public Boolean runDetectCross = true;
    
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

        // ----------------------- ONLY TO BE CALCULATED ONCE ---------------------------
        orgMatrix = new Mat();
        modMatrix = new Mat();

        // Saving the input from the camera capture to the new matrix
		capture.read(orgMatrix);
		modMatrix = orgMatrix.clone();

        // Running color detection
        Mat isolatedRedColor = new Mat();
        isolatedRedColor = runColorDetection(orgMatrix);

        // Dynamic corner detection
        squareCorners = dynamicCornerDetection(isolatedRedColor);

        // Neutralization of the corner points
        squareCorners.set(0, calculateActualCoordinates(squareCorners.get(0), "edge"));
        squareCorners.set(1, calculateActualCoordinates(squareCorners.get(1), "edge"));
        squareCorners.set(2, calculateActualCoordinates(squareCorners.get(2), "edge"));
        squareCorners.set(3, calculateActualCoordinates(squareCorners.get(3), "edge"));

        // Print informations to GUI
        doFrameReprint(orgMatrix, modMatrix, isolatedRedColor);
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

        // Running color detection
        Mat isolatedRedColor = new Mat();
        isolatedRedColor = runColorDetection(orgMatrix);

        // Create a new outline for the obstacle course
        printOutlineToOrigImg(squareCorners);

        // Running ball detection method.
        findBalls(orgMatrix, isolatedRedColor, arrayMap);
        
        // Delete balls outside of square | Do this before calibration
        evaluateBallLocation();

        // Detect Cross Coordinates
        if(runDetectCross) {
            detectCross(isolatedRedColor);
            runDetectCross = !runDetectCross;
        }
        
        // Detect balls within cross, and their safe pickup
        isInside(listOfBallCoordinates, 100);
        calculateSafePoints();

        /*
        // Calculating "actual" coordinates for each ball.
        for (int i = 0; i < listOfBallCoordinates.size(); i++) {
        	Point temp = new Point();
        	temp = calculateActualCoordinates(listOfBallCoordinates.get(i), "ball");
        	listOfBallCoordinates.set(i, temp);
		}
		*/

        // Estimating Robot Coordinates based on image from webcam
        robotCameraPoints = newRobotDetect(orgMatrix);

        // Calculating the actual coordinates of the first robot marker
        robotBlueMarker = calculateActualCoordinates(robotCameraPoints[0], "robot");
        robotGreenMarker = calculateActualCoordinates(robotCameraPoints[1], "robot");
        
        doFrameReprint(orgMatrix, modMatrix, isolatedRedColor);
  	}

	
	private void detectCross(Mat isolatedRed) 
	{
		crossPoints.clear();
		Mat isolatedRedLocal = new Mat();
		isolatedRedLocal = isolatedRed.clone();
        MatOfPoint corners = new MatOfPoint();
        double qualityLevel = 0.06; // 0.01 org
        double minDistance = 68;
        int blockSize = 15;
        int gradientSize = 15;
        double k = 0.04;
        
        int p1x = (int) getCorners().get(0).x;
        int p1y = (int) getCorners().get(0).y;
        int p4x = (int) getCorners().get(3).x;
        int p4y = (int) getCorners().get(3).y;

        Rect rectCrop = new Rect(new Point(p1x, p1y), new Point(p4x, p4y));
        Mat isolatedRedLocalCropped = isolatedRedLocal.submat(rectCrop);

    	Imgproc.equalizeHist(isolatedRedLocalCropped, isolatedRedLocalCropped);
        Imgproc.goodFeaturesToTrack(blur(isolatedRedLocalCropped, 10), corners, 4, qualityLevel, minDistance, new Mat(),
                    blockSize, gradientSize, false, k);


        int[] cornersData = new int[(int) (corners.total() * corners.channels())];
        corners.get(0, 0, cornersData);


        for (int i = 0; i < corners.rows(); i++) {
        	crossPoints.add(new Point(cornersData[i * 2]+p1x, cornersData[i * 2 + 1]+p1y));
        }

        for (int i = 0; i < corners.rows(); i++) {
        	Imgproc.circle(modMatrix, new Point(cornersData[i * 2]+p1x, cornersData[i * 2 + 1]+p1y), 3, new Scalar(200, 200, 200), Core.FILLED);
        }

        if (corners.rows() == 4) 
        {
        	makeCircle();

	        //listOfCircleBalls = isInside(listOfBallCoordinates, 100);
	        //calculateSafePoints();
        }
        else {
        	// Try again
        	crossPoints.clear();
        }
	}

	
	private void calculateSafePoints()
	{
		for (int i = 0; i < listOfCircleBalls.size(); i++) {
			listOfCircleBallsPickup.add(new Point());
		}
		
		for (int i = 0; i < listOfCircleBalls.size(); i++)
		{
			double minDist = 10000000;
			int smallestDistance = 0;
			Point closePoint1 = new Point();
			Point closePoint2 = new Point();
			
			List<Point> tempCrossPoints = new ArrayList<>();
			tempCrossPoints.addAll(crossPoints);
			
			for (int j = 0; j < tempCrossPoints.size(); j++) 
			{
				double tempDist = calculateDistanceBetweenPoints(tempCrossPoints.get(j).x, tempCrossPoints.get(j).y, listOfCircleBalls.get(i).x, listOfCircleBalls.get(i).y);
				if (tempDist < minDist) {
					minDist = tempDist;
					closePoint1 = tempCrossPoints.get(j);
					smallestDistance = j;
				} 
			}
			
			tempCrossPoints.remove(smallestDistance);
			minDist = 10000000;
			
			for (int j = 0; j < tempCrossPoints.size(); j++) 
			{
				double tempDist = calculateDistanceBetweenPoints(tempCrossPoints.get(j).x, tempCrossPoints.get(j).y, listOfCircleBalls.get(i).x, listOfCircleBalls.get(i).y);
				if (tempDist < minDist) {
					minDist = tempDist;
					closePoint2 = tempCrossPoints.get(j);
				}
			}
			
			Imgproc.circle(modMatrix, closePoint1, 3, new Scalar(0, 128, 255), Core.FILLED);
			Imgproc.circle(modMatrix, closePoint2, 3, new Scalar(0, 0, 255), Core.FILLED);
									
			double distanceRatio = 0.5;
	    	Point betweenCrossLegs = new Point(
	    			((1-distanceRatio)*closePoint1.x + distanceRatio*closePoint2.x) ,
	    			((1-distanceRatio)*closePoint1.y + distanceRatio*closePoint2.y) );
			
	    	Imgproc.circle(modMatrix, betweenCrossLegs, 5, new Scalar(255, 255, 255), Core.FILLED);
	    	Point crossCenter = getCrossCenterPoint();
	    	
	    	distanceRatio = 3;
	    	Point safePickupPoint = new Point(
	    			((1-distanceRatio)*crossCenter.x + distanceRatio*betweenCrossLegs.x) ,
	    			((1-distanceRatio)*crossCenter.y + distanceRatio*betweenCrossLegs.y) );

	    	Imgproc.circle(modMatrix, safePickupPoint, 5, new Scalar(255, 255, 255));

	    	listOfCircleBallsPickup.set(i, safePickupPoint);
	    	
		}
	}

	
	public void makeCircle() {
		int radius = 100;

		double meanx = (crossPoints.get(0).x + crossPoints.get(1).x + crossPoints.get(2).x + crossPoints.get(3).x)/4;
		double meany = (crossPoints.get(0).y + crossPoints.get(1).y + crossPoints.get(2).y + crossPoints.get(3).y)/4;

		Imgproc.circle(modMatrix, new Point(meanx, meany), radius, new Scalar(255,255,0)); // CYAN

	}

	
	public void isInside(List<Point> balls, double rad)
	{
		double meanx = (crossPoints.get(0).x + crossPoints.get(1).x + crossPoints.get(2).x + crossPoints.get(3).x)/4;
		double meany = (crossPoints.get(0).y + crossPoints.get(1).y + crossPoints.get(2).y + crossPoints.get(3).y)/4;

		listOfCircleBalls.clear();
		Point circle = new Point(meanx, meany);

		for (int i = 0; i < listOfBallCoordinates.size(); i++)
		{
			if (calculateDistanceBetweenPoints(listOfBallCoordinates.get(i).x, listOfBallCoordinates.get(i).y, circle.x, circle.y) < rad) {
				listOfCircleBalls.add(listOfBallCoordinates.get(i));
			}
		}
		
		for (int i = 0; i < listOfCircleBalls.size(); i++) {
			Imgproc.circle(modMatrix, listOfCircleBalls.get(i), 8, new Scalar(0,255,255), Core.FILLED); // YELLOW
		}				
	}

	
	public Mat blur(Mat input, int numberOfTimes){
        Mat sourceImage = new Mat();
        Mat destImage = input.clone();

        for(int i=0;i<numberOfTimes;i++){
            sourceImage = destImage.clone();
            Imgproc.medianBlur(sourceImage, destImage, 3);
            Imgproc.blur(sourceImage, destImage, new Size(3.0, 3.0));
        }
        return destImage;
    }

	
	public static double calculateAngle(double x1, double y1, double x2, double y2)
	{
	    double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
	    // Keep angle between 0 and 360
	    angle = angle + Math.ceil( -angle / 360 ) * 360;

	    return angle;
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
        //Mat print = new Mat(imageHeight, imageWidth, CvType.CV_8U);

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
    	localPoint = inputPoint;				// Making sure that global variables isn't modified before value is returned.

    	// Calculating how many pixels it takes to get a mm.
    	double mmToPixel = 2.34; 				// We have calculated the value to 1,7 at center and 2,34 at the corner

        // Measurements (camera, robot, ball and obstacles)
        double cameraHeight = 2000 / 2; 		// 2000mm
        double robotHeight = 300; 				// 300mm
        double ballHeight = 25;					// 40mm
        double courseEdgeHeight = 74;			// 70mm
        double crossHeight = 30;				// 30mm
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
    private static void findBalls(Mat localOrgMatrix, Mat isolatedRedColor, int[][] localMap)
    {
    	Mat src = new Mat();
    	src = localOrgMatrix.clone();

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
                (double) gray.rows() / 50,  	// change this value to detect circles with different distances to each other (orig: 8)
                25.0,
                20.0,
                9, 								// Minimum radius
                13);           					// Maximum radius
        										// change the last two parameters (orig: 1 , 10)
        										// Latest calibration : 8, 15)
        										// Eclipse calibration : 9, 11)

        /* PLEASE DO NOT DELETE !!! ------------------------------------ !!!
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
         */
        
        listOfBallCoordinates.clear();
        
        for (int x = 0; x < circles.cols(); x++)
        {
            double[] c = circles.get(0, x);

            // Calculation center of the circle
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));

            // Calculation radius of the circle
            double radius = Math.round(c[2]);

            // Adding ball coordinates to the list of ball center coordinates
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
        } // End of for loop for each detected circle
    } // End of private static void runOpenCV(...)


	private static void evaluateBallLocation()
	{
		Point VT = new Point(squareCorners.get(0).x,squareCorners.get(0).y);
		Point VB = new Point(squareCorners.get(1).x,squareCorners.get(1).y);
		Point HT = new Point(squareCorners.get(2).x,squareCorners.get(2).y);
		Point HB = new Point(squareCorners.get(3).x,squareCorners.get(3).y);

		Rect rVTHB = new Rect(HB, VT);
		Rect rVBHT = new Rect(HT, VB);

		List<Point> validBalls = new ArrayList<>();

		for (Point p : listOfBallCoordinates) {
			if(rVTHB.contains(p)) {
				validBalls.add(p);
			} else if (rVBHT.contains(p)) {
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
        Scalar max = new Scalar(80, 100, 255, 0);  		// BGR-A (NOT RGB!) (Better than original : (140, 110, 255, 0))

        // Initializing color range
        inRange(frame, min, max, frame);

        // Adding some blur to the image, so that "mistakes" is washed out
        Imgproc.blur(frame, frame, new Size(3,3), new Point(-1,-1));

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

        return draw;
    }

    
    private static boolean checkDistance(double temp_vt, double distance_vt) {
        if (temp_vt < distance_vt) {
            return true;
        }
        return false;
    }


    public double calculateDistanceBetweenPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
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

    
    public static BufferedImage Mat2BufferedImage(Mat matrix)throws IOException {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

    
	public void doFrameReprint(Mat orgMatrix2, Mat modMatrix2, Mat isolatedRedColor2) {
		label1.setIcon(new ImageIcon(new ImageIcon(HighGui.toBufferedImage(modMatrix2)).getImage().getScaledInstance(label1.getWidth(), label1.getHeight(), Image.SCALE_DEFAULT)));
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

        // Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	

	private double angle(Point p1, Point p2, Point p0)
	{
		double dx1 = p1.x - p0.x;
		double dy1 = p1.y - p0.y;
		double dx2 = p2.x - p0.x;
		double dy2 = p2.y - p0.y;
		return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}

	
	private Mat cropOrgMatrix() 
	{
    	Mat orgMatrixClone = new Mat();
    	orgMatrixClone = orgMatrix.clone();

    	int startX = (int) squareCorners.get(0).x;
    	int startY = (int) squareCorners.get(0).y;
    	int width = (int) squareCorners.get(3).x - (int) squareCorners.get(0).x;
    	int height = (int) squareCorners.get(1).y - (int) squareCorners.get(0).y;

		Rect rectCrop = new Rect(startX,startY,width,height);

		Mat croppedImage = new Mat(orgMatrixClone, rectCrop);

    	Imgproc.rectangle(orgMatrixClone, squareCorners.get(0), squareCorners.get(2), new Scalar(0, 255, 0, 255), 3);

    	return orgMatrixClone;
	}

	public List<Point> getCorners() {
		return squareCorners;
	} // End of getCorners()

	public List<Point> getCrossPoints()	{
		return crossPoints;
	} // End of getCrossPoints()
	
	public List<Point> getCrossBalls() {
		return listOfCircleBalls;
	} // End of getCrossBalls()
	
	public List<Point> getCrossBallsPickup() {
		return listOfCircleBallsPickup;
	} // End of getCrossBallsPickup()

	public Point getCrossCenterPoint() {
		double meanx = 0.0;
		double meany = 0.0;

		if (!crossPoints.isEmpty())	{
			meanx = (crossPoints.get(0).x + crossPoints.get(1).x + crossPoints.get(2).x + crossPoints.get(3).x)/4;
			meany = (crossPoints.get(0).y + crossPoints.get(1).y + crossPoints.get(2).y + crossPoints.get(3).y)/4;
		}
		return new Point (meanx,meany);
	} // End of getCrossCenterPoint()

} // End of public class Billedbehandling
