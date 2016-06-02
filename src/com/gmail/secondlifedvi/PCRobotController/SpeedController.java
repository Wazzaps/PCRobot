package com.gmail.secondlifedvi.PCRobotController;

@SuppressWarnings("unused")
public abstract class SpeedController {
	private Robot robot;
	private int myID;
	
	public abstract void set(double value);
}
