package com.gmail.secondlifedvi.PCRobotController;

public class Relay {
	private Robot robot;
	private int myID;

	public Relay(Robot robot, int port) {
		this.robot = robot;
		myID = robot.relayIncrement++;
		robot.connection.sendRaw("relay:new:" + myID + ":" + port);
	}

	public void set(int value) {
		if (value > 0)
			value = 1;
		if (value < 0)
			value = -1;
		robot.connection.sendRaw("relay:set:" + myID + ":" + value);
	}
}
