package com.gmail.secondlifedvi.PCRobotController;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class VisionUtils {
	public static MatOfPoint detectObject(Mat processedImage, double minSize) {
		Mat workingMat = processedImage.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(workingMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		// Find biggest contour
		int biggestContour = -1;
		double contourSize = minSize;
		for (int i = 0; i < contours.size(); i++) {
			if (Imgproc.contourArea(contours.get(i)) > contourSize) {
				biggestContour = i;
				contourSize = Imgproc.contourArea(contours.get(i));
			}
		}
		if (biggestContour != -1) {
			return contours.get(biggestContour);
		} else {
			return null;
		}
	}

	public static Mat threshholdBetween(Mat input, int lo, int hi) {
		Mat A = new Mat();
		Mat B = new Mat();
		Mat C = new Mat();
		Imgproc.threshold(input, A, lo, 255, Imgproc.THRESH_BINARY);
		Imgproc.threshold(input, B, hi, 255, Imgproc.THRESH_BINARY_INV);
		Core.bitwise_and(A, B, C);
		return C;
	}

	public static Mat threshold3Channels(Mat input, int aLo, int aHi, int bLo, int bHi, int cLo, int cHi) {
		List<Mat> channels = new ArrayList<Mat>();
		Core.split(input, channels);
		Mat A = threshholdBetween(channels.get(0), aLo, aHi);
		Mat B = threshholdBetween(channels.get(1), bLo, bHi);
		Mat C = threshholdBetween(channels.get(2), cLo, cHi);
		Core.bitwise_and(A, B, A);
		Core.bitwise_and(A, C, A);
		return A;
	}

	public static Mat colorHighlight(Mat original, Mat mask) {
		try {
			Mat grayscale = new Mat(original.width(), original.height(), CvType.CV_8UC1);
			Imgproc.cvtColor(original, grayscale, Imgproc.COLOR_BGR2GRAY);
			Mat mask3 = new Mat();
			Imgproc.cvtColor(mask, mask3, Imgproc.COLOR_GRAY2BGR);
			
			Mat a = new Mat();
			Mat b = new Mat();
			Imgproc.cvtColor(grayscale, a, Imgproc.COLOR_GRAY2BGR);
			Core.multiply(a, new Scalar(0.3, 0.3, 0.3) , a);
			original.copyTo(b);
			Core.bitwise_not(mask3, mask3);
			
				Core.bitwise_and(a, mask3, a);
			
			Core.bitwise_not(mask3, mask3);
			Core.bitwise_and(b, mask3, b);
			Core.add(a, b, a);
			return a;
		} catch (Exception e) {
			
		}
		return original;
	}
	
	public static void objectRectangle (Mat src, MatOfPoint contour, Scalar color) {
		Rect rect = Imgproc.boundingRect(contour);
		Imgproc.rectangle(src, new Point(rect.x, rect.y),
				new Point(rect.x + rect.width, rect.y + rect.height), color, 2);
	}
}
