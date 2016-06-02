package com.gmail.secondlifedvi.PCRobotPrograms;

import java.text.DecimalFormat;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.gmail.secondlifedvi.PCRobotController.CANTalon;
import com.gmail.secondlifedvi.PCRobotController.DoubleDrive;
import com.gmail.secondlifedvi.PCRobotController.Drive;
import com.gmail.secondlifedvi.PCRobotController.VisionUtils;
import com.gmail.secondlifedvi.PCRobotController.Robot;
import com.gmail.secondlifedvi.PCRobotController.Solenoid;
import com.gmail.secondlifedvi.PCRobotController.TCPClient;
import com.gmail.secondlifedvi.PCRobotController.Victor;

/**
 * David shlemayev's ball throwing robot. A working example of my PCRobot
 * library and contestant for the 2016 Technoda's Young Inventors competition.
 * Works on 1573's 2016 robot that was sent to the competition.
 * 
 * Instructions:
 *   - Launch after connecting to the robot, and opening the driver station
 *   - Make sure the dashboard (if open) doesn't try to use the camera
 *   - Run this program, and make sure you see camera input
 *   - Press enable
 * 
 * @author David shlemayev
 */

public class AimBot {
	// Tracks the last time the ball was fired, so it won't attempt to fire multiple times in a row
	static long lastFired = 0;
	
	// Decides if you should look for a ball (false) or a target (true)
	static boolean hasBall = false;
	
	// The actuators are controlled by another thread, main loop shouldn't be controlling anything
	static boolean fixedControl = false;
	
	static double barea = 0;
	
	// Width of area that the ball is allowed to be in, in order to fire
	final static int targetWidth = 100;

	// Point of entrance for the program
	public static void main(String[] args) {
		// Create new robot class
		Robot robot = new Robot();

		// Tell the robot class your team number so it can connect to the robot.
		robot.connection = new TCPClient(robot, 1573);

		// Connect to the webcam on the robot to process it's images
		robot.startCamera();

		// Open an image window to display the webcam's feed and processed images, with a panel size of 570x428 and 2 image panels
		robot.openWindow(570, 428, 2);

		// Create actuators (talons, solenoids, compressors)
		robot.startCompressor(1);
		CANTalon right1 = new CANTalon(robot, 2, false);
		CANTalon right2 = new CANTalon(robot, 3, false);
		CANTalon left1 = new CANTalon(robot, 4, true);
		CANTalon left2 = new CANTalon(robot, 5, true);
		Victor liftingMotor = new Victor(robot, 3, false);
		Victor shootingMotor = new Victor(robot, 2, false);
		Solenoid fireTrigger = new Solenoid(robot, 1, 0);

		DoubleDrive drive = new DoubleDrive(new Drive(right1, left1), new Drive(right2, left2));
		//Drive drive = new Drive(right1, left1);

		// Execute program until closed
		while (true) {
			if (!robot.isNewImageReady()) {
				// The robot won't make new decisions if the image is the same, so no point in running the logic
				continue;
			}

			// The camera might return an empty image, so don't crash over it
			try {
				// Convert camera image into a format that is easier to work with (Hue-Luminance-Saturation, HLS for short)
				Mat targetHLS = new Mat(robot.cameraImage.width(), robot.cameraImage.height(), CvType.CV_8UC1);
				Imgproc.cvtColor(robot.cameraImage, targetHLS, Imgproc.COLOR_BGR2HLS);
				if (hasBall) {
					// Find target
					
					// Mat stands for Matrix, a 2D array of numbers, used for storing images
					// VisionUtils.threshold3Channels differentiates between what is a part target and what isnt
					Mat targetThreshold = VisionUtils.threshold3Channels(targetHLS, 40, 77, 48, 203, 35, 166);
					// VisionUtils.colorHighlight creates an image where the target is highlighted, used for tuning the detection parameters
					Mat colorHighlighted = VisionUtils.colorHighlight(robot.cameraImage, targetThreshold);
					// VisionUtils.detectObject finds the biggest group of pixels (contour, MatOfPoint) from the threshhold method
					MatOfPoint targetContour = VisionUtils.detectObject(targetThreshold, 100);
					// If a target exists
					if (targetContour != null) {
						// Get a rectangle which encompasses the target
						Rect rect = Imgproc.boundingRect(targetContour);
						
						// Draw the rectangle on the images for easier understanding of the program's current "thought"
						VisionUtils.objectRectangle(robot.cameraImage, targetContour, new Scalar(255, 100, 0));
						VisionUtils.objectRectangle(colorHighlighted, targetContour, new Scalar(255, 100, 0));

						// Get the area which the target takes on the screen, used as an approximation of the distance to the target
						double area = Imgproc.contourArea(targetContour);

						// Logic variables
						double r = 0; // Rotation
						double m = 0; // Movement
						double s = 0.6; // Global speed modifier
						boolean A = false; // Rotation is correct
						boolean B = false; // Forward/Backward is correct

						// If close then slow down (Seems backwards?)
						// TODO: Fix this area
						/*
						if (area < 700) {
							s = 0.4;
						} else if (area < 1000) {
							s = 0.6;
						} else {
							s = 1;
						}*/
						
						// If the middle of the target is too much to the left/right, move in that direction
						// Visualization of the function:
						// https://www.desmos.com/calculator/kfdwwxelsi
						// something * 1.0 is a int -> float conversion
						r = -(((rect.x + rect.width / 2) / (robot.getCameraWidth() * 1.0)) - 0.5) * 2;
						double rDirection = r >= 0 ? 1 : -1;
						if (r > -0.03 && r < 0.03) {
							rDirection = 0;
							B = true;
						}
						r = Math.max(Math.abs(r), 0.3) * rDirection;
						
						// If the area of the target (and therefore the distance) is out of the bounds, drive to correct that
						if (area > 1000) {
							m = -0.6;
						} else if (area < 650) {
							m = 0.6;
						} else {
							A = true;
						}
						
						// If all conditions are met, then the control loop can be disabled and a seperate firing thread can be started.
						if (A && B && lastFired + 6000 < System.currentTimeMillis()) {
							lastFired = System.currentTimeMillis();
							new Thread("FireThread") {
								public void run() {
									delay(1.5);
									liftingMotor.set(1);
									shootingMotor.set(1);
									delay(0.5);
									fixedControl = true;
									fireTrigger.set(true);
									delay(0.5);
									fireTrigger.set(false);
									liftingMotor.set(0);
									shootingMotor.set(0);
									delay(0.5);
									drive.arcade(-0.5, 0);
									setText(robot, -0.5, 0);
									delay(0.5);
									drive.arcade(0, 0);
									setText(robot, 0, 0);
									hasBall = false;
									// Let the ball fall off
									delay(2);
									fixedControl = false;
								}
							}.start();
						}

						if (!fixedControl) {
							setText(robot, m * s, -r * s);
							drive.arcade(m * s, -r * s);
						}
					} else {
						// Target isn't found, search.
						setText(robot, 0, 0.4);
						drive.arcade(0, 0.4);
					}
					// Show the processed image on the window
					robot.setWindowImage(0, robot.cameraImage);
					robot.setWindowImage(1, colorHighlighted);
				} else {
					// Find ball
					
					// Same as above
					Mat ballStationThreshold = VisionUtils.threshold3Channels(targetHLS, 134, 172, 61, 235, 46, 255);
					MatOfPoint targetContour = VisionUtils.detectObject(ballStationThreshold, 100);
					Mat humanHighlighted = VisionUtils.colorHighlight(robot.cameraImage, ballStationThreshold);
					
					// If a ball station is found
					if (targetContour != null) {
						// Draw the rectangle on the images for easier understanding of the program's current "thought"
						VisionUtils.objectRectangle(robot.cameraImage, targetContour, new Scalar(255, 100, 0));
						VisionUtils.objectRectangle(humanHighlighted, targetContour, new Scalar(255, 100, 0));

						// Drive to ball
						if (!fixedControl) {
							Rect rect = Imgproc.boundingRect(targetContour);
							double area = Imgproc.contourArea(targetContour);
							barea = area;
							// Logic variables
							double r = 0; // Rotation
							double m = 0; // Movement
							double s = 0.7; // Global speed modifier
							
							// Rotation control
							r = -(((rect.x + rect.width / 2) / (robot.getCameraWidth() * 1.0)) - 0.5) * 2;

							// Movement and pickup based on area (distance)
							if (area > 9000) {
								// Bug, ignore
								continue;
							} else if (area > 6300) {
								// Pickup
								fixedControl = true;
								new Thread("PickupThread") {
									public void run() {
										drive.arcade(0, 0);
										delay(0.5);
										drive.arcade(0.25, 0);
										setText(robot, 0.25, 0);
										liftingMotor.set(1);
										delay(1.5);
										drive.arcade(0, 0);
										setText(robot, 0, 0);
										liftingMotor.set(1);
										delay(4);
										drive.arcade(-0.25, 0);
										setText(robot, -0.25, 0);
										delay(1.5);
										drive.arcade(0, 0);
										setText(robot, 0, 0);
										liftingMotor.set(0);
										fixedControl = false;
										hasBall = true;
									}
								}.start();
								continue;
							} else if (area > 3000) {
								m = 0.3;
							} else {
								m = 0.45;
							}
							setText(robot, m * s, -r * s);
							drive.arcade(m * s, -r * s);

						}
					} else if (!fixedControl) {
						// Look for ball station
						setText(robot, 0, 0.4);
						drive.arcade(0, 0.4);
					}

					// Show the processed image on the window
					robot.setWindowImage(0, robot.cameraImage);
					robot.setWindowImage(1, humanHighlighted);
				}
				
				// Re-draw the window with the updated images
				robot.updateWindow();
			} catch (Exception e) {
				// Sometimes images bug out
				e.printStackTrace();
			}
		}
	}

	static void delay(double d) {
		try {
			Thread.sleep((long) (d * 1000));
		} catch (InterruptedException e) {

		}
	}

	static void setText(Robot robot, double m, double r) {
		DecimalFormat format = new DecimalFormat("#0.000");
		// Set the text on the right side of the window
		robot.setWindowText("Area: "+barea+"\nHas ball: " + hasBall + "\nMove: " + format.format(m) + "\nRotate: " + format.format(r));
	}
}
