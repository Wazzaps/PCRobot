package com.gmail.secondlifedvi.PCRobotPrograms;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.gmail.secondlifedvi.PCRobotController.CANTalon;
import com.gmail.secondlifedvi.PCRobotController.DoubleDrive;
import com.gmail.secondlifedvi.PCRobotController.Drive;
import com.gmail.secondlifedvi.PCRobotController.Robot;
import com.gmail.secondlifedvi.PCRobotController.Solenoid;
import com.gmail.secondlifedvi.PCRobotController.TCPClient;

public class RobotProgram {
	public static void main (String[] args) {
		System.out.println("Welcome to the Example Robot program!");
		// Create new robot class
		Robot robot = new Robot();
		
		// Tell the robot class your team number so it can connect to the robot.
		robot.connection = new TCPClient(robot, 1573);
		
		// Connect to the webcam on the robot to process it's images
		robot.startCamera();
		
		// Open an image window to display the webcam's feed and processed images, with an image size of 360x270
		// (only supported size right now), and 2 image panels
		robot.openWindow(360, 270, 2);
		
		// Create actuators (talons, solenoids, compressors)
		robot.startCompressor(1);
		CANTalon right1 = new CANTalon (robot, 2, false);
	    CANTalon right2 = new CANTalon (robot, 3, false);
	    CANTalon left1 = new CANTalon (robot, 4, true);
	    CANTalon left2 = new CANTalon (robot, 5, true);
	    Solenoid fireTrigger = new Solenoid (robot, 1, 0);
	    
	    DoubleDrive drive = new DoubleDrive(new Drive(right1, left1), new Drive(right2, left2));
		
		// Execute program until closed
		while (true) {
			// Show the original image on the first panel
			robot.setWindowImage(0, robot.cameraImage);
			
			/* Image processing */
			// Create a new "Mat" (Matrix, image container) for the processed image
			Mat grayscale = new Mat(robot.cameraImage.width(), robot.cameraImage.height(), CvType.CV_8UC1);
			// Convert the webcam's image to grayscale and put it into the 'grayscale' variable
			Imgproc.cvtColor(robot.cameraImage, grayscale, Imgproc.COLOR_BGR2GRAY);
			// Show the processed image on the window
			robot.setWindowImage(1, grayscale);
			robot.updateWindow();
			
			// Actuators can be used here
			drive.arcade(0, 0);
			fireTrigger.set(false);
			
			// Show text on screen for debugging purposes
			robot.setWindowText("Hello world!\nThis is and example");
		}
	}
}
