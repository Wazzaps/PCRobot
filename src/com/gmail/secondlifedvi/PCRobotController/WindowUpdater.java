package com.gmail.secondlifedvi.PCRobotController;

public class WindowUpdater implements Runnable {
	Robot robot;
	
	@Override
	public void run() {
		while (true) {
			if (robot.newImageAvailable) {
				//robot.updateWindow();
			}
		}
	}
	
	public WindowUpdater (Robot robot) {
		this.robot = robot;
	}

}
