package com.gmail.secondlifedvi.PCRobotController;

public class AnalogInput {
	protected double value = 0;
	private int myID;

	public AnalogInput(Robot robot, int port) {
		myID = robot.analogInputs.size();
		robot.analogInputs.add(0, this);
		robot.connection.sendRaw("analoginput:new:" + myID + ":" + port);
	}

	public double get() {
		return value;
	}
}
