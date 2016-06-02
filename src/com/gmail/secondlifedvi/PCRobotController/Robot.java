package com.gmail.secondlifedvi.PCRobotController;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Robot {
	// Actuators
	protected int speedControllerIncrement = 0;
	protected int solenoidIncrement = 0;
	protected int relayIncrement = 0;
	protected List<AnalogInput> analogInputs = new ArrayList<AnalogInput>();
	protected boolean newImageAvailable = false;
	protected long lastImageTime = 0;

	// Load OpenCv for image processing
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("OpenCV loaded with version " + Core.VERSION);
	}

	// Camera
	private Thread cameraThread;
	public Mat cameraImage = new Mat(240, 320, CvType.CV_8UC3);

	// Window
	private Window cameraWindow;

	// Connection
	public TCPClient connection;

	public boolean isNewImageReady() {
		if (newImageAvailable) {
			newImageAvailable = false;
			return true;
		} else {
			return false;
		}
	}

	protected static void cleanup() {

	}

	public void setWindowText(String content) {
		cameraWindow.setText("<html>"+content.replace("\n", "<br>")+"</html>");
	}

	// Information for robot programs
	public int getCameraWidth() {
		return cameraImage.width();
	}

	public int getCameraHeight() {
		return cameraImage.height();
	}

	public void setWindowImage(int i, Mat img) {
		cameraWindow.setImage(i, img);
	}

	public void setWindowImage(int i, BufferedImage img) {
		cameraWindow.setImage(i, img);
	}

	public void updateWindow() {
		cameraWindow.update();
	}

	protected void imageReady(Mat image) {
		cameraImage = image;
	}

	protected void restartCamera() {
		cameraThread = new Thread(new RobotWebCamera(connection.host, this));
		cameraThread.start();
	}

	public void startCamera() {
		restartCamera();
	}

	public void openWindow(int imageWidth, int imageHeight, int numOfViews) {
		cameraWindow = new Window(numOfViews, imageWidth, imageHeight);
		Thread windowUpdater = new Thread(new WindowUpdater(this));
		windowUpdater.start();
	}

	public void startCompressor(int pcmPort) {
		connection.sendRaw("compressor:" + pcmPort);
	}
}
