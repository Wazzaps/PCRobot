package com.gmail.secondlifedvi.PCRobotController;

public class Victor extends SpeedController {
	private Robot robot;
	private int myID;

	public Victor(Robot robot, int canID, boolean inverted) {
		this.robot = robot;
		myID = robot.speedControllerIncrement++;
		robot.connection.sendRaw("victor:new:" + myID + ":" + canID + ":" + inverted);
	}
	
	public void set(double value) {
		robot.connection.sendRaw("victor:set:" + myID + ":" + value);
	}
}
