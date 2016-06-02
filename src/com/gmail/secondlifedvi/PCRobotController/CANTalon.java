package com.gmail.secondlifedvi.PCRobotController;

public class CANTalon extends SpeedController {
	private Robot robot;
	private int myID;
	private boolean inverted;
	private boolean setThisFrame = false;
	private long lastFrameTime = 0;

	public CANTalon(Robot robot, int canID, boolean inverted) {
		this.robot = robot;
		myID = robot.speedControllerIncrement++;
		this.inverted = inverted;
		robot.connection.sendRaw("cantalon:new:" + myID + ":" + canID + ":false");
	}
	
	public void set(double value) {
		if (robot.lastImageTime != lastFrameTime) {
			setThisFrame = false;
			lastFrameTime = robot.lastImageTime;
		}
		if (setThisFrame) {
			//System.out.println("Warning: Motor value set twice before new camera frame recieved");
			//Thread.dumpStack();
		}
		setThisFrame = true;
		robot.connection.sendRaw("cantalon:set:" + myID + ":" + (inverted ? -value : value));
	}
}
