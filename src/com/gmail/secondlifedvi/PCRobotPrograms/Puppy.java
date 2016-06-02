package com.gmail.secondlifedvi.PCRobotPrograms;

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
import com.gmail.secondlifedvi.PCRobotController.TCPClient;

public class Puppy {
	final static int targetWidth = 50;

	public static void main(String[] args) {
		// Create new robot class
		Robot robot = new Robot();

		// Tell the robot class your team number so it can connect to the robot.
		robot.connection = new TCPClient(robot, 1573);

		// Connect to the webcam on the robot to process it's images
		robot.startCamera();

		// Open an image window to display the webcam's feed and processed images, with an image size of 360x270
		// (only supported size right now), and 2 image panels
		robot.openWindow(360, 270, 3);

		// Create actuators (talons, solenoids, compressors)
		robot.startCompressor(1);
		CANTalon right1 = new CANTalon(robot, 2, false);
		CANTalon right2 = new CANTalon(robot, 3, false);
		CANTalon left1 = new CANTalon(robot, 4, true);
		CANTalon left2 = new CANTalon(robot, 5, true);
		DoubleDrive drive = new DoubleDrive(new Drive(right1, left1), new Drive(right2, left2));
		//Drive drive = new Drive(right1, left1);

		// Execute program until closed
		while (true) {
			if (!robot.isNewImageReady()) {
				continue;
			}

			/* Image processing */

			try {
				// Create a new "Mat" (Matrix, image container) for the processed image

				Mat targetHLS = new Mat(robot.cameraImage.width(), robot.cameraImage.height(), CvType.CV_8UC1);
				Imgproc.cvtColor(robot.cameraImage, targetHLS, Imgproc.COLOR_BGR2HLS);
				Mat targetThreshold = VisionUtils.threshold3Channels(targetHLS, 40, 150, 20, 170, 40, 218);
				Mat colorHighlighted = VisionUtils.colorHighlight(robot.cameraImage, targetThreshold);
				MatOfPoint targetContour = VisionUtils.detectObject(targetThreshold, 100);
				if (targetContour != null) {
					Rect rect = Imgproc.boundingRect(targetContour);
					VisionUtils.objectRectangle(targetThreshold, targetContour, new Scalar(200, 200, 200));
					VisionUtils.objectRectangle(robot.cameraImage, targetContour, new Scalar(0, 0, 255));
					VisionUtils.objectRectangle(colorHighlighted, targetContour, new Scalar(255, 0, 255));

					double area = Imgproc.contourArea(targetContour);

					// We have a target!

					double r = 0; // Rotation
					double m = 0; // Movement
					double s = 0.7; // Global speed modifier

					/*if (area < 3000) {
						s = 0.4;
					} else if (area < 2500) {
						s = 0.6;
					} else {
						s = 1;
					}*/

					if (rect.x + rect.width / 2 < (robot.getCameraWidth() - targetWidth) / 2) {
						r = 0.6;
					} else if (rect.x + rect.width / 2 > (robot.getCameraHeight() + targetWidth) / 2) {
						r = -0.6;
					}

					r = -(((rect.x + rect.width / 2) / (robot.getCameraWidth() * 1.0)) - 0.5) * 2;
					robot.setWindowText("" + area);
					if (area > 7500) {
						m = -0.4;
					} else if (area < 6200) {
						m = 0.4;
					}

					drive.arcade(m * s, -r * s);
				} else {
					drive.arcade(0, 0.4);
				}

				// Show the processed image on the window
				robot.setWindowImage(0, robot.cameraImage);
				robot.setWindowImage(1, colorHighlighted);
				robot.setWindowImage(2, targetThreshold);
				robot.updateWindow();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	static void delay(double d) {
		try {
			Thread.sleep((long) (d * 1000));
		} catch (InterruptedException e) {

		}
	}
}
