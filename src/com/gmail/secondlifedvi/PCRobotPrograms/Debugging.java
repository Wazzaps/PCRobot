package com.gmail.secondlifedvi.PCRobotPrograms;

import com.gmail.secondlifedvi.PCRobotController.AnalogInput;
import com.gmail.secondlifedvi.PCRobotController.Relay;
import com.gmail.secondlifedvi.PCRobotController.Robot;
import com.gmail.secondlifedvi.PCRobotController.TCPClient;
import com.gmail.secondlifedvi.PCRobotController.Victor;

public class Debugging {
	public static void main (String[] args) {
		// Configure robot
		Robot robot = new Robot();
		robot.connection = new TCPClient(robot, 1573);
		robot.startCamera();
		robot.openWindow(570, 428, 1);
		
		// Things to debug
		AnalogInput hasBall = new AnalogInput(robot, 1);
		Relay liftingMotor = new Relay(robot, 0);
		Victor shootingMotor = new Victor(robot, 5, false);
		
		liftingMotor.set(-1);
		// Execute program until closed
		while (true) {
			//drive.arcade(whiteLine.get() > 2 ? 0 : 0.3, 0);
			//liftingMotor.set(whiteLine.get() > 2 ? -1 : 0);
			try {
				if (hasBall.get() > 1.9) {
					liftingMotor.set(0);
					Thread.sleep(500);
					shootingMotor.set(1);
					Thread.sleep(2000);
					liftingMotor.set(-1);
					Thread.sleep(2000);
					shootingMotor.set(0);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//shootingMotor.set(hasBall.get() > 1.9 ? 1 : 0);
			robot.setWindowImage(0, robot.cameraImage);
			robot.setWindowText("ball: "+(hasBall.get()));
			robot.updateWindow();
		}
	}
}
