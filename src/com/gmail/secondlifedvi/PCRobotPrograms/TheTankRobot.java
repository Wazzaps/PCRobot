package com.gmail.secondlifedvi.PCRobotPrograms;

import java.text.DecimalFormat;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.gmail.secondlifedvi.PCRobotController.AnalogInput;
import com.gmail.secondlifedvi.PCRobotController.DoubleDrive;
import com.gmail.secondlifedvi.PCRobotController.Drive;
import com.gmail.secondlifedvi.PCRobotController.Relay;
import com.gmail.secondlifedvi.PCRobotController.VisionUtils;
import com.gmail.secondlifedvi.PCRobotController.Robot;
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

public class TheTankRobot {
	// Tracks the last time the ball was fired, so it won't attempt to fire multiple times in a row
	static long lastFired = 0;

	// The actuators are controlled by another thread, main loop shouldn't be controlling anything
	static boolean trackingTarget = true;
	
	static double lastBallSensorValue = 0;

	// If to look for another ball after firing
	static boolean loop = true;
	
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
		Victor right1 = new Victor(robot, 1, true);
		Victor right2 = new Victor(robot, 2, true);
		Victor left1 = new Victor(robot, 3, false);
		Victor left2 = new Victor(robot, 4, false);
		Relay liftingMotor = new Relay(robot, 0);
		Victor shootingMotor = new Victor(robot, 5, false);
		AnalogInput hasBall = new AnalogInput(robot, 1);

		DoubleDrive drive = new DoubleDrive(new Drive(right1, left1), new Drive(right2, left2));
		//Drive drive = new Drive(right1, left1);

		// Execute program until closed
		while (true) {
			if (!robot.isNewImageReady()) {
				// The robot won't make new decisions if the image is the same, so no point in running the logic
				continue;
			}
			lastBallSensorValue = hasBall.get();

			// The camera might return an empty image, so don't crash over it
			try {
				// Convert camera image into a format that is easier to work with (Hue-Luminance-Saturation, HLS for short)
				Mat targetHLS = new Mat(robot.cameraImage.width(), robot.cameraImage.height(), CvType.CV_8UC1);
				Imgproc.cvtColor(robot.cameraImage, targetHLS, Imgproc.COLOR_BGR2HLS);

				// Find target

				// Mat stands for Matrix, a 2D array of numbers, used for storing images
				// VisionUtils.threshold3Channels differentiates between what is a part target and what isnt
				Mat targetThreshold = VisionUtils.threshold3Channels(targetHLS, 40, 77, 48, 190, 35, 166);
				// VisionUtils.colorHighlight creates an image where the target is highlighted, used for tuning the detection parameters
				Mat colorHighlighted = VisionUtils.colorHighlight(robot.cameraImage, targetThreshold);
				// VisionUtils.detectObject finds the biggest group of pixels (contour, MatOfPoint) from the threshhold method
				MatOfPoint targetContour = VisionUtils.detectObject(targetThreshold, 100);

				if (trackingTarget) {
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
						double s = 0.7; // Global speed modifier
						boolean A = false; // Forward/Backward is correct
						boolean B = false; // Rotation is correct

						// If the middle of the target is too much to the left/right, move in that direction
						// Visualization of the function:
						// https://www.desmos.com/calculator/kfdwwxelsi
						// something * 1.0 is a int -> double conversion
						r = (((rect.x + rect.width / 2) / (robot.getCameraWidth() * 1.0)) - 0.5) * 2;
						double rDirection = r >= 0 ? 1 : -1;
						if (r > -0.03 && r < 0.03) {
							rDirection = 0;
							B = true;
						}
						r = Math.max(Math.abs(r), 0.2) * rDirection;

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

							if (hasBall.get() > 1.9) {
								// Fire the ball
								new Thread("FireThread") {
									public void run() {
										liftingMotor.set(1);
										delay(1);
										liftingMotor.set(0);
										delay(1);
										shootingMotor.set(1);
										delay(2);
										liftingMotor.set(-1);
										trackingTarget = false;
										delay(1);
										while (hasBall.get() > 1.9) {
											delay(0.01);
										}
										delay(0.5 );
										shootingMotor.set(0);
										liftingMotor.set(0);
										
										if (loop) {
											trackingTarget = true;
										}
									}
								}.start();
							} else {
								// Pickup
								new Thread("PickupThread") {
									public void run() {
										delay(1);
										trackingTarget = false;
										drive.arcade(0.5, 0);
										liftingMotor.set(-1);
										while (!(hasBall.get() > 1.9)) {
											delay(0.01);
										}
										drive.arcade(0, 0);
										delay(1);
										drive.arcade(-0.7, 0);
										liftingMotor.set(0);
										delay(3);
										trackingTarget = true;
									}
								}.start();
							}

						} else {
							setText(robot, m * s, -r * s);
							drive.arcade(m * s, -r * s);
						}
					} else {
						drive.arcade(0, 0.4);
					}
				}

				
				// Show the processed image on the window
				robot.setWindowImage(0, robot.cameraImage);
				robot.setWindowImage(1, colorHighlighted);


				// Re-draw the window with the updated images
				robot.updateWindow();
			} catch (Exception e) {
				// Sometimes images bug out
				// Ignore
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
		robot.setWindowText("Tracking: "+trackingTarget+"\nBall: " + (lastBallSensorValue > 1.9) + "("+lastBallSensorValue+")\nMove: " + format.format(m) + "\nRotate: " + format.format(r));
	}
}
