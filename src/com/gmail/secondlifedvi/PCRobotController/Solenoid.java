package com.gmail.secondlifedvi.PCRobotController;

public class Solenoid {
	private Robot robot;
	private int myID;

	public Solenoid(Robot robot, int pcmPort, int port) {
		this.robot = robot;
		myID = robot.solenoidIncrement++;
		robot.connection.sendRaw("solenoid:new:" + myID + ":" + pcmPort + ":" + port);
	}

	public void set(boolean on) {
		robot.connection.sendRaw("solenoid:set:" + myID + ":" + (on ? "true" : "false"));
	}
}
