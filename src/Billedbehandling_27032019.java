import org.jfree.chart.block.GridArrangement;
import org.opencv.core.*;
import org.opencv.core.Point;
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
import java.util.Scanner;
 
import javax.imageio.ImageIO;
import javax.sound.sampled.Line;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
 
import static org.opencv.core.Core.inRange;

public class Billedbehandling_27032019
{
    // The camera has a maximum resolution of 1920x1080
    static int imageWidth = 1920;                		// Image width
    static int imageHeight = 1080;             			// Image height
    
    // Color range for detecting RED
    static Scalar min = new Scalar(0, 0, 150, 0);      	// BGR-A (NOT RGB!) (Better than original : (0, 0, 130, 0))
    static Scalar max = new Scalar(80, 100, 255, 0);  	// BGR-A (NOT RGB!) (Better than original : (140, 110, 255, 0))
    
    // Color range for detecting BLUE circle on robot
    static Scalar minBlue = new Scalar(90, 0, 0, 0);  	// BGR-A (NOT RGB!)
    static Scalar maxBlue = new Scalar(255, 70, 65, 0); // BGR-A (NOT RGB!)
    
    // Color range for detecting GREEN circle on robot
    static Scalar minGreen = new Scalar(0, 100, 0, 0);	// BGR-A (NOT RGB!)
    static Scalar maxGreen = new Scalar(90, 255, 90, 0);// BGR-A (NOT RGB!)
 
    // Measurements (camera, robot, ball and obstacles)
	static double cameraHeight = 2000; 					// 2000mm = 200cm
	static double robotHeight = 280; 					// 280mm = 28cm
	static double ballHeight = 40;						// 40mm = 4cm
	static double courseEdgeHeight = 70;				// 70mm = 7cm
	static double crossHeight = 30;						// 30mm = 3cm
	static Point imageCenter = new Point(imageWidth/2, imageHeight/2);
    
	// Niklas
	
    private Mat src = new Mat();
    private Mat srcGray = new Mat();
    private JFrame frame;
    private JLabel imgLabel;
    private static final int MAX_THRESHOLD = 100;
    private int maxCorners = 36;
    private Random rng = new Random(12345);
	
    // Instantiating the imgcodecs class
    static Imgcodecs imageCodecs = new Imgcodecs();
 
    // Compulsory
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
 
    public static void main(String[] args)
    {
    	// Creating a two-dimensional array
        int[][] arrayMap = new int[imageHeight][imageWidth];
        
        // Creating an array of points
        Point[] robotCameraPoints = new Point[2];
        Point[] robotActualPoints = new Point[2];
        
        // Initializing video capture | the image needs to be in a 1920x1080 form factor
        System.out.println("| --------------- Video Capture activated -------------- |");
        
        VideoCapture capture = new VideoCapture(1);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, imageWidth);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, imageHeight);
                
        // Adjusting autofocus - no guarantee that the camera can do it
        //capture.set(Videoio.CAP_PROP_AUTOFOCUS, 1);   // Autofocus on
        //capture.set(Videoio.CAP_PROP_AUTOFOCUS, 0);   // Autofocus off
 
        // Keyboard initialization
        Scanner keyboard = new Scanner(System.in);
 
        // Creating new matrix to hold image information
        Mat matrix = new Mat();
 
        // Control guide - must be the same as the condition in while
        System.out.println("        ------ Press 1 to capture new image ------        ");
 
        // Boolean to enable console comments and camera
    	Boolean enableComments = false;
    	Boolean enableCamera = false;
    	
        // Defining default file along with file name
        String default_file = "C:\\Users\\benja\\Desktop\\test_orig.png";
        String filename = ((args.length > 0) ? args[0] : default_file);
        
        // The detection program only runs when the user has pressed 1
        while(keyboard.nextInt() == 1)
        {        	
        	if(enableCamera.equals(true)) {
	            // Saving the input from the camera capture to the new matrix
	    		capture.read(matrix);
        	}
 
        	if (enableCamera.equals(false)) {
    	    	// Load an image
    	        matrix = Imgcodecs.imread(filename, Imgcodecs.IMREAD_COLOR);
    	        // Check if image is loaded correctly
    	        if (matrix.empty()) {
    	            System.out.println("Error opening image!");
    	            System.out.println("Program Arguments: [image_name -- default " + default_file + "] \n");
    	            System.exit(-1);
    	        }
        	}
        	
            // Specifying path for where to save image
        	if(enableComments) System.out.println("Creating file : test_orig.png");
            //String file = "C:\\Users\\benja\\Desktop\\test_orig.png";
 
            // Saving the original RGB image without any modifications
            if(enableComments) System.out.println("Saving RGB image to : test_orig.png");
            imageCodecs.imwrite(default_file, matrix);
            
            /*
            // Estimating Robot Coordinates based on image from webcam
            robotCameraPoints = robotCircleCenter(matrix, default_file);
            
            // Calculating the actual coordinates of the first robot marker
            robotActualPoints[0] = calculateRobotCoordinates(robotCameraPoints[0], "robot");
            
            // Calculating the actual coordinates of the second robot marker
            robotActualPoints[1] = calculateRobotCoordinates(robotCameraPoints[1], "robot");
            */
            
            findPixelSize(filename);
            
            // Run color detection
            if(enableComments) System.out.println("Running color detection : saved as test1.png");
            Mat isolatedRedColor = new Mat();
            isolatedRedColor = runColorDetection(matrix);
 
            // Edge detection
            if(enableComments) System.out.println("Running edge detection : saved as test1_edges.png");
            String edgeFile = "C:\\Users\\benja\\Desktop\\test_1_edges.png";
            runEdgeDetection(isolatedRedColor, edgeFile);
            
            // Running detection function.
            if(enableComments) System.out.println("Running circel detection : saved as test2.png");
            findBalls(filename, default_file, isolatedRedColor, arrayMap);
            
            // Create a matrix similar to the modified picture
            if(enableComments) System.out.println("Accessing create_matrix() - example image saved as test3.png");
            //arrayMap = create_matrix(arrayMap);
            
            System.out.println("| ------------------------ Done ------------------------ |");
            System.out.println("        ------ Press 1 to capture new image ------        ");
            
        } // End of while
    } // End of main
 
    /**
     * Takes the original image and isolates the colors blue and green. 
     * The center of these colored circles is then determined and returned as a list of points.
     * 
     * It is the users responsibility that only two circles is detected (one for each color)
     * 
     * @param localColorFrame
     * @param filename
     * @param default_file
     * @return circleCenter[]
     */
    private static Point[] robotCircleCenter(Mat localColorFrame, String default_file) 
    {    	
    	// Read from camera or filename
    	Boolean readFromCamera = false;
    	
    	// Creating Mat's to hold information from image.
    	Mat frameBlue = new Mat();
    	Mat frameGreen = new Mat();
    	
        // Creating new matrix to hold the detected circles
        Mat circlesBlue = new Mat();
        Mat circlesGreen = new Mat();
    	
    	// Cloning the original color image into two individual 2D arrays.
    	frameBlue = localColorFrame.clone();
    	frameGreen = localColorFrame.clone();
    	
    	/*
    	if (!readFromCamera) 
    	{
	    	// Load an image
	        Mat src = Imgcodecs.imread(filename, Imgcodecs.IMREAD_COLOR);
	 
	        // Check if image is loaded correctly
	        if (src.empty()) {
	            System.out.println("Error opening image!");
	            System.out.println("Program Arguments: [image_name -- default " + default_file + "] \n");
	            System.exit(-1);
	        }
	 
	    	// Cloning the original color image into two individual 2D arrays.
	    	frameBlue = src.clone();
	    	frameGreen = src.clone();
    	}
    	*/
    	
        // Initializing color range
        inRange(frameBlue, minBlue, maxBlue, frameBlue);
        inRange(frameGreen, minGreen, maxGreen, frameGreen);
        
        // Adding some blur to the image, so that "mistakes" is washed out
        Imgproc.blur(frameBlue, frameBlue, new Size(3,3), new Point(-1,-1));	
        Imgproc.blur(frameGreen, frameGreen, new Size(3,3), new Point(-1,-1));
 
        // Adding some blur to the image to smooth out edges
        Imgproc.medianBlur(frameBlue, frameBlue, 7);
        Imgproc.medianBlur(frameGreen, frameGreen, 7);
 
        // Saving the image path and writing the new image
        String fileTestB = "C:\\Users\\benja\\Desktop\\IdentifyBlue.png";
        imageCodecs.imwrite(fileTestB, frameBlue);
        String fileTestG = "C:\\Users\\benja\\Desktop\\IdentifyGreen.png";
        imageCodecs.imwrite(fileTestG, frameGreen);
        
        // Detecting circles from the grayscale image and saving it in the circles matrix
        Imgproc.HoughCircles(frameBlue, 
        		circlesBlue, 
        		Imgproc.HOUGH_GRADIENT, 
        		1.0,
                (double) frameBlue.rows() / 1, 	// change this value to detect circles with different distances to each other (orig: 8)
                25.0, 
                14.0, 
                10, 							// Minimum radius
                15);           					// Maximum radius
                
        Imgproc.HoughCircles(frameGreen, 
        		circlesGreen, 
        		Imgproc.HOUGH_GRADIENT, 
        		1.0,
                (double) frameGreen.rows() / 1,	// change this value to detect circles with different distances to each other (orig: 8)
                25.0, 
                14.0, 
                10, 							// Minimum radius
                15);           					// Maximum radius
                
        // Creating an array of points to hold the two circle center coordinates
        Point[] centerPoint = new Point[2];
        
        // Calculating the center of the blue circle
        double[] cB = circlesBlue.get(0, 0);
        centerPoint[0] = new Point(Math.round(cB[0]), Math.round(cB[1]));
        
        // Calculating the center of the green circle
        double[] cG = circlesGreen.get(0, 0);
        centerPoint[1] = new Point(Math.round(cG[0]), Math.round(cG[1]));
        
        // Creating two new images/mats/arrays with the camera specifications (width/height)
        Mat printBlue = new Mat(imageHeight, imageWidth, CvType.CV_8U);
        Mat printGreen = new Mat(imageHeight, imageWidth, CvType.CV_8U);
        
        // Printing all of detected circles onto the new clean blue mat.
        for (int x = 0; x < circlesBlue.cols(); x++)
        {
            double[] c = circlesBlue.get(0, x);
            
            // Calculation center of the circle
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            
            // Calculation radius of the circle
            int radius = (int) Math.round(c[2]);
            
            // Used to write information to the image already containing information about the colored barriers
            Imgproc.circle(printBlue,       // Circle center
                    center, 
                    1,
                    new Scalar(255, 255, 255),
                    1,
                    0,
                    0);
 
            Imgproc.circle(printBlue,      	// Circle outline
                    center,
                    radius,
                    new Scalar(255, 255, 255),
                    2,
                    8,
                    0);
            
            // ################# TESTING THE CONSQUENCE OF CHANGING COORDINATE VALUES #################
            // This test prints a big white dot at the coordinates specified by the testCenter point.
            // I have used this test to observe how the x- and y-coordinates work in a grid.
            /*
            Point testCenter = new Point(990,540);
            Imgproc.circle(printBlue,       // Circle center
                    testCenter, 
                    1,
                    new Scalar(255, 255, 255),
                    5,
                    0,
                    0);
            */
            // ################# END OF TESTING #################

            
            // Saving the image path and writing the new image
            String fileBlue = "C:\\Users\\benja\\Desktop\\final_Blue.png";
            imageCodecs.imwrite(fileBlue, printBlue);
        }
        
        // Printing all of detected circles onto the new clean green mat.
        for (int x = 0; x < circlesGreen.cols(); x++)
        {
            double[] c = circlesGreen.get(0, x);
            
            // Calculation center of the circle
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            
            // Calculation radius of the circle
            int radius = (int) Math.round(c[2]);
            
            // Used to write information to the image already containing information about the colored barriers
            Imgproc.circle(printGreen,       	// Circle center
                    center, 
                    1,
                    new Scalar(255, 255, 255),
                    1,
                    0,
                    0);
 
            Imgproc.circle(printGreen,      	// Circle outline
                    center,
                    radius,
                    new Scalar(255, 255, 255),
                    2,
                    8,
                    0);
            // Saving the image path and writing the new image
            //String file = "C:\\Users\\benja\\Desktop\\test_Green.png";
            String fileGreen = "C:\\Users\\benja\\Desktop\\final_Green.png";
            imageCodecs.imwrite(fileGreen, printGreen);
        }
        
        return centerPoint;
        
    } // End of findRobotCoordinates(...)
    
    /**
     * The method takes as input a center point of the circle on the robot,
     * detected by the camera. The function then returns the actual values of this point.
     * Taken into account, that the height of the robot and angel from the camera 
     * can have an effect on the points location on the grid.
     * @param localPoint
     * @param objectType
     * @return point
     */
    private static Point calculateRobotCoordinates(Point localPoint, String objectType) 
    {    	
    	// Boolean to enable console comments
    	Boolean enableComments = true;
    	
    	// Calculating how many pixels it takes to get a mm.
    	double mmToPixel = 1.7;
    	
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
    	
    	// Returning the calculated robot coordinate
    	return pointToBeReturned;
    } // End of robotCalculateCoordinates()
    
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
     * @param frameColor
     */
    private static int[][] findBalls(String filename, String default_file, Mat frameColor, int[][] localMap)
    {
        // Load an image
        Mat src = Imgcodecs.imread(filename, Imgcodecs.IMREAD_COLOR);
 
        // Check if image is loaded correctly
        if (src.empty()) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default " + default_file + "] \n");
            System.exit(-1);
        }
 
        // Creating new matrix to hold grayscale image information
        Mat gray = new Mat();
 
        // Converting the original image (src) into an grayscale image and saving it as grey
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
 
        // Adding some blur to the image to smooth out edges
        Imgproc.medianBlur(gray, gray, 5);
 
        // Creating new matrix to hold the detected circles
        Mat circles = new Mat();
 
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
  
        for (int x = 0; x < circles.cols(); x++)
        {
            double[] c = circles.get(0, x);
            
            // Calculation center of the circle
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            
            // Calculation radius of the circle
            double radius = Math.round(c[2]);            
            
            // Parsing a double value to an integer
            localMap[(int)center.y][(int)center.x] = 2;
 
            Imgproc.circle(src,            		// Circle center
                    center,
                    1,
                    new Scalar(0, 100, 100),
                    3,
                    8,
                    0);
             
            Imgproc.circle(src,              	// Circle outline
                    center,
                    (int)radius,
                    new Scalar(255, 0, 255),
                    3,
                    8,
                    0);
 
            // ---------------------------------------------------------------------------------------------------------
            // Used to write information to the image already containing information about the colored barriers
            Imgproc.circle(frameColor,       	// Circle center
                    center, 
                    1,
                    new Scalar(255, 255, 255),
                    0,
                    0,
                    0);
 
            Imgproc.circle(frameColor,      	// Circle outline
                    center,
                    (int)radius,
                    new Scalar(255, 255, 255),
                    1,
                    8,
                    0);
 
            // Saving the image path and writing the new image
            String file = "C:\\Users\\benja\\Desktop\\test1.png";
            imageCodecs.imwrite(file, frameColor);
            // ---------------------------------------------------------------------------------------------------------
        } // End of for loop for each detected circle

        return localMap;

    } // End of private static void runOpenCV(...)
    

    /**
     * Takes a frame as input and returns an matrix with only the red color highlighted
     * @param frame
     */
    private static Mat runColorDetection(Mat frame)
    {
        // Initializing color range
        inRange(frame, min, max, frame);
 
        // Adding some blur to the image, so that "mistakes" is washed out
        Imgproc.blur(frame, frame, new Size(3,3), new Point(-1,-1));
 
        // Saving the image path and writing the new image
        String file = "C:\\Users\\benja\\Desktop\\test1.png";
        imageCodecs.imwrite(file, frame);
 
        return frame;
    }
 
    /**
     * Takes the image with the color detected edges and
     * isolates the edges of the red parts of the trakc.
     * @param frame
     * @param file
     */
    private static void runEdgeDetection(Mat frame, String file)
    {
        //Mat temp_mat = Imgcodecs.imread(default_file, 1);
 
        Mat gray = new Mat();
        Mat draw = new Mat();
        Mat wide = new Mat();
 
        int threshold1 = 10;
        int threshold2 = 150;
        int apertureSize = 3;
        boolean L2gradient = false;
 
        /*
        Scanner scan = null;
        try {
            scan = new Scanner(new File("C:\\Users\\benja\\Desktop\\config.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
 
        while(scan.hasNextLine())
        {
            threshold1 = Integer.parseInt(scan.nextLine());
            threshold2 = Integer.parseInt(scan.nextLine());
            apertureSize = Integer.parseInt(scan.nextLine());
 
            String line = scan.nextLine();
            if (line.equals("false")) {
                L2gradient = false;
            }
            else if (line.equals("true")) {
                L2gradient = true;
            }
        }
        */
 
        //Imgproc.blur(temp_mat, gray, new Size(3,3));
        Imgproc.Canny(frame, wide, threshold1, threshold2, apertureSize, L2gradient);
        wide.convertTo(draw, CvType.CV_8U);
 
        // Saving the calculated matrix to the given path name (file)
        Imgcodecs.imwrite(file, draw);
    }

 
    /**
     * Creates a matrix from the image containing both color and circular detection
     * The function creates an image as output and saves it on the computer
     */
    private static int[][] create_matrix(int[][] localMap)
    {
        Mat imgMat = new Mat( imageHeight, imageWidth, CvType.CV_8U );   // CvType.CV_8U : Unsigned 8bit (the same as an image pixel)
        BufferedImage bi = null;
 
        try {
            bi = ImageIO.read(new File("C:\\Users\\benja\\Desktop\\test1.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load input image");
        }
 
        int[] pixel;
        //int[][] localMap = new int[imageWidth][imageHeight];
 
        for (int row = 0; row < bi.getHeight(); row++) {
            for (int col = 0; col < bi.getWidth(); col++) {
                pixel = bi.getRaster().getPixel(col, row, new int[3]); // Brug new int[3] hvis sort/hvid (grÃ¥)-billede. Brug int[4] hvis farve
 
                if (pixel[0] == 255) {
                    //System.out.println(pixel[0] + " - " + pixel[1] + " - " + pixel[2] + " - " + (bi.getWidth() * y + x));
                    //System.out.println(x + ", " + y);
                	localMap[col][row] = 1;        	// An edge
                    imgMat.put(row, col, 255);    	// Prints a white spot on the Mat to check that the two-dimensional array is correct
                }
                
                if (pixel[0] == 0 && pixel[1] == 255 && pixel[2] == 0) {
                	localMap[col][row] = 2;        	// A ball
                    imgMat.put(row, col, 255);    	// Prints a white spot on the Mat to check that the two-dimensional array is correct
                }
                
            }
        }
 
        // Saving the image path
        String file = "C:\\Users\\benja\\Desktop\\test3-matrix-output.png";
        imageCodecs.imwrite(file, imgMat);
 
        return localMap;
        
    } // End of create_matrix()
    
    public void RunUpdate(String[] args) 
    {
        String filename = args.length > 0 ? args[0] : "C:\\Users\\Niklas\\Desktop\\test_1_edges.png";
        src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        // Create and set up the window.
        frame = new JFrame("Shi-Tomasi corner detector demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set up the content pane.
        Image img = HighGui.toBufferedImage(src);
        addComponentsToPane(frame.getContentPane(), img);
        // Use the content pane's default BorderLayout. No need for
        // setLayout(new BorderLayout());
        // Display the window.
        frame.pack();
        frame.setVisible(true);
        update();
    }
    
    private void update() {
        Mat videoframe = new Mat();
        //0; default video device id
        VideoCapture camera = new VideoCapture(0);

        /*
        while (true) {
            if (camera.read(videoframe)) {

                ImageIcon image = new ImageIcon(Mat2bufferedImage(frame));


            }
        }
        */

        maxCorners = Math.max(maxCorners, 1);
        MatOfPoint corners = new MatOfPoint();
        double qualityLevel = 0.01;
        double minDistance = 10;
        int blockSize = 3, gradientSize = 3;
        boolean useHarrisDetector = false;
        double k = 0.04;
        Mat copy = src.clone();
        Imgproc.goodFeaturesToTrack(srcGray, corners, maxCorners, qualityLevel, minDistance, new Mat(),
                blockSize, gradientSize, useHarrisDetector, k);
        System.out.println("** Number of corners detected: " + corners.rows());
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
        for (int i = 0; i < corners.rows(); i++) {

            // Tilføjer points til listen
            List<Point> PointLIST = new ArrayList<>();
            PointLIST.add(distancepoint_vt);
            PointLIST.add(distancepoint_vb);
            PointLIST.add(distancepoint_ht);
            PointLIST.add(distancepoint_hb);


            double temp_vt = Math.hypot(500-cornersData[i * 2], 275-cornersData[i * 2 + 1]);
            double temp_vb = Math.hypot(485-cornersData[i * 2], 850-cornersData[i * 2 + 1]);
            double temp_ht = Math.hypot(1360-cornersData[i * 2], 238-cornersData[i * 2 + 1]);
            double temp_hb = Math.hypot(1360-cornersData[i * 2], 850-cornersData[i * 2 + 1]);

            if (checkDistance(temp_vt, distance_vt))
            {
                distance_vt = temp_vt;
                distancepoint_vt = new Point(cornersData[i * 2], cornersData[i * 2 + 1]);
                PointLIST.set(0, distancepoint_vt);
            }

            if (checkDistance(temp_vb, distance_vb))
            {
                distance_vb = temp_vb;
                distancepoint_vb = new Point(cornersData[i * 2], cornersData[i * 2 + 1]);
                PointLIST.set(1, distancepoint_vb);
            }

            if (checkDistance(temp_ht, distance_ht))
            {
                distance_ht = temp_ht;
                distancepoint_ht = new Point(cornersData[i * 2], cornersData[i * 2 + 1]);
                PointLIST.set(2, distancepoint_ht);
            }

            if (checkDistance(temp_hb, distance_hb))
            {
                distance_hb = temp_hb;
                distancepoint_hb = new Point(cornersData[i * 2], cornersData[i * 2 + 1]);
                PointLIST.set(3, distancepoint_hb);
            }

        }
        Imgproc.circle(copy, distancepoint_vt, radius,
                new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);
        Imgproc.circle(copy, distancepoint_ht, radius,
                new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);
        Imgproc.circle(copy, distancepoint_vb, radius,
                new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);
        Imgproc.circle(copy, distancepoint_hb, radius,
                new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Core.FILLED);

        imgLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(copy)));
        frame.repaint();
    }

    private boolean checkDistance(double temp_vt, double distance_vt) {
        if (temp_vt < distance_vt)
        {
            return true;
        }
        return false;
    }
    
    private void addComponentsToPane(Container pane, Image img) {
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
    
} // End of public class Billedbehandling