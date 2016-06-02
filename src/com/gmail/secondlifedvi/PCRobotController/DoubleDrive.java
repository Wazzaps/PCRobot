package com.gmail.secondlifedvi.PCRobotController;

// When your chassis needs two motors per side (4x4)
public class DoubleDrive {
	private Drive drive1;
	private Drive drive2;

	public DoubleDrive(Drive drive1, Drive drive2) {
		this.drive1 = drive1;
		this.drive2 = drive2;
	}

	public void arcade(double movement, double rotation) {
		drive1.arcade(movement, rotation);
		drive2.arcade(movement, rotation);
	}
}
