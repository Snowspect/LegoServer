import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
 
import java.io.*;
import java.util.Scanner;
 
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
 
import static org.opencv.core.Core.inRange;
 
public class Billedbehandling_27032019
{
    // The camera has a maximum resolution of 1920x1080
    public static int imageWidth = 1920;                // Image width
    public static int imageHight = 1080;                // Image hight
 
    // Color range for detecting RED
    //static Scalar min = new Scalar(0, 0, 130, 0);       // BGR-A (NOT RGB!) (Original)
    //static Scalar max = new Scalar(140, 110, 255, 0);   // BGR-A (NOT RGB!)
 
    static Scalar min = new Scalar(0, 0, 150, 0);       // BGR-A (NOT RGB!) (Better than original)
    static Scalar max = new Scalar(80, 100, 255, 0);     // BGR-A (NOT RGB!)
 
    // Instantiating the imgcodecs class
    static Imgcodecs imageCodecs = new Imgcodecs();
 
    // Compulsory
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
 
    public static void main(String[] args)
    {
        // Initializing video capture | the image needs to be in a 1920x1080 "formfactor"
        System.out.println("| --------------- Video Capture activated -------------- |");
 
        VideoCapture capture = new VideoCapture(1);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH, imageWidth);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, imageHight);
 
        // Adjusting autofocus - no guarantee that the camera can do it
        //capture.set(Videoio.CAP_PROP_AUTOFOCUS, 1);   // Autofocus on
        //capture.set(Videoio.CAP_PROP_AUTOFOCUS, 0);   // Autofocus off
 
        // Keyboard initialization
        Scanner keyboard = new Scanner(System.in);
 
        // Creating new matrix to hold image information
        Mat matrix = new Mat();
 
        // Control guide - must be the same as the condition in while
        System.out.println("        ------ Press 1 to capture new image ------        ");
 
        // The detection program only runs when the user has pressed 1
        while(keyboard.nextInt() == 1)
        {
            // Saving the input from the camera capture to the new matrix
            capture.read(matrix);
 
            // Specifying path for where to save image
            System.out.println("Creating file : test_orig.png");
            String file = "C:\\Users\\benja\\Desktop\\test_orig.png";
 
            // Saving the original RGB image without any modifications
            System.out.println("Saving RGB image to : test_orig.png");
            imageCodecs.imwrite(file, matrix);
 
            // Run color detection
            System.out.println("Running color detection : saved as test1.png");
            Mat frameColor = new Mat();
            frameColor = runColorDetection(matrix);
 
            // Edge detection
            System.out.println("Running edge detection : saved as test1_edges.png");
            String edgeFile = "C:\\Users\\benja\\Desktop\\test_1_edges.png";
            //String default_file = "C:\\Users\\benja\\Desktop\\test1.png";
            runEdgeDetection(frameColor, edgeFile);
 
            // Defining default file along with file name
            String default_file = "C:\\Users\\benja\\Desktop\\test_orig.png";
            String filename = ((args.length > 0) ? args[0] : default_file);
 
            // Running detection function.
            System.out.println("Running circel detection : saved as test2.png");
            runOpenCV(filename, default_file, frameColor);
 
            // Create a matrix similar to the modified picture
            System.out.println("Accessing create_matrix() - example image saved as test3.png");
            create_matrix();
 
        } // End of while
    } // End of main
 
    /**
     * Takes a frame as input and updates the output picture
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
 
        // Opening GUI window to illustrate the detected edges.
        //HighGui.imshow("detected circles", draw);
        //HighGui.waitKey(1);
    }
 
    /**
     * The function runOpenCV takes a file for the original image.
     * a file that specifies where to save the new image and a matrix
     * containing the output from the color detection function.
     * @param filename
     * @param default_file
     * @param frameColor
     */
    private static void runOpenCV(String filename, String default_file, Mat frameColor)
    {
        // Load an image
        Mat src = Imgcodecs.imread(filename, Imgcodecs.IMREAD_COLOR);
 
        // Check if image is loaded correctly
        if (src.empty()) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default "
                    + default_file + "] \n");
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
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double) gray.rows() / 25,                              // change this value to detect circles with different distances to each other (orig: 8)
                25.0, 14.0, 8, 15);           // change the last two parameters (orig: 1 , 10)
                                                                                // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++)
        {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
 
            Imgproc.circle(src,                     // Circle center
                    center,
                    1,
                    new Scalar(0, 100, 100),
                    3,
                    8,
                    0);
 
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(src,                     // Circle outline
                    center,
                    radius,
                    new Scalar(255, 0, 255),
                    3,
                    8,
                    0);
 
            // ---------------------------------------------------------------------------------------------------------
            // Used to write information to the image already containing information about the barriers color
            Imgproc.circle(frameColor,                // Circle center
                    center, 1,
                    new Scalar(255, 255, 255),
                    0,
                    0,
                    0);
 
            Imgproc.circle(frameColor,                // Circle outline
                    center,
                    radius,
                    new Scalar(255, 255, 255),
                    1,
                    8,
                    0);
 
            // Saving the image path and writing the new image
            String file = "C:\\Users\\benja\\Desktop\\test2.png";
            imageCodecs.imwrite(file, frameColor);
            // ---------------------------------------------------------------------------------------------------------
        } // End of for loop for each detected circle
 
        // Opening GUI window to illustrate the detected circles.
        //HighGui.imshow("detected circles", src);
        //HighGui.waitKey();
    }
 
    /**
     * Creates a matrix from the image containing both color and circular detection
     * The function creates an image as output and saves it on the computer
     */
    private static void create_matrix()
    {
        Mat imgMat = new Mat( imageHight, imageWidth, CvType.CV_8U );   // CvType.CV_8U : Unsigned 8bit (the same as a img pixel)
        BufferedImage bi = null;
 
        try {
            bi = ImageIO.read(new File("C:\\Users\\benja\\Desktop\\test2.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load input image");
        }
 
        int[] pixel;
        int[][] matrix = new int[imageWidth][imageHight];
 
        for (int row = 0; row < bi.getHeight(); row++) {
            for (int col = 0; col < bi.getWidth(); col++) {
                pixel = bi.getRaster().getPixel(col, row, new int[4]); // Brug new int[3] hvis sort/hvid (grå)-billede.
 
                if (pixel[0] == 255) {
                    //System.out.println(pixel[0] + " - " + pixel[1] + " - " + pixel[2] + " - " + (bi.getWidth() * y + x));
                    //System.out.println(x + ", " + y);
                    matrix[col][row] = 1;               // An edge
                    imgMat.put(row, col, 255);    // Prints a white spot on the Mat to check that the two-dimensional array is correct
                }
                if (pixel[0] == 0 && pixel[1] == 255 && pixel[2] == 0) {
                    matrix[col][row] = 2;               // A ball
                    imgMat.put(row, col, 255);    // Prints a white spot on the Mat to check that the two-dimensional array is correct
                }
            }
        }
 
        // Saving the image path
        String file = "C:\\Users\\benja\\Desktop\\test3-matrix-output.png";
        imageCodecs.imwrite(file, imgMat);
 
        System.out.println("| ------------------------ Done ------------------------ |");
        System.out.println("        ------ Press 1 to capture new image ------        ");
 
    } // End of create_matrix()
} // End of public class Billedbehandling