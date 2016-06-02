PCRobot
=======

This is the client/library counterpart to the [PCRobot client](https://github.com/Wazzaps/PCRobotClient).

[Video](https://www.youtube.com/watch?v=_ttVocVCmsk)

Installation
------------
* Make sure your robot has the PCRobot client deployed
* Copy this project to your workspace, and import it in eclipse
* Right click the project in eclipse and click Build path -> Configure Build Path
* Double-click OpenCv
* Click "User libraries"
* Double-click "opencv_310.jar"
* Select the jar in the "opencv" directory in the root of this project
* Double-click "Native library location" -> "External folder" and select the folder in the "opencv" folder corresponding to your architecture (x86 or x64)
* Check out the examples at the "PCRobotPrograms" package (RobotProgram is a very basic program, Aimbot is the one in the video)

Example
-------
```java
// Create new robot class
Robot robot = new Robot();

// Tell the robot class your team number so it can connect to the robot.
robot.connection = new TCPClient(robot, 1573);

// Connect to the webcam on the robot to process it's images
robot.startCamera();

// Open an image window to display the webcam's feed and processed images, with an image size of 360x270, and 2 image panels
robot.openWindow(360, 270, 2);

// Create actuators (talons, solenoids, compressors)
robot.startCompressor(1);
CANTalon right1 = new CANTalon (robot, 2, false);
CANTalon right2 = new CANTalon (robot, 3, false);
CANTalon left1 = new CANTalon (robot, 4, true);
CANTalon left2 = new CANTalon (robot, 5, true);
Solenoid fireTrigger = new Solenoid (robot, 1, 0);

// 4x4 driving
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
	drive.arcade(0.4, 0);
	fireTrigger.set(false);
	
	// Show text on screen for debugging purposes
	robot.setWindowText("Hello world!\nThis is and example");
}
```
Licence
-------
This software is licenced under the Mozilla Public License Version 2.0 licence
See the LICENCE file for more information
Also refer to the OpenCV licence at opencv/LICENCE