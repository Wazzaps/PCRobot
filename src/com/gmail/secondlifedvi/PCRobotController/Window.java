package com.gmail.secondlifedvi.PCRobotController;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class Window {
	private JFrame window;
	private JPanel horizontalPanel;
	private JLabel debugging;
	private JLabel[] label;
	private ImageIcon[] icon;
	public Mat[] currentImage;
	
	private Dimension imageDimension = new Dimension(360, 270);
	private Size imageSize = new Size(imageDimension.getWidth(), imageDimension.getHeight());
	
	public Window (int numOfImages, int sizeX, int sizeY) {
		imageDimension = new Dimension(sizeX, sizeY);
		imageSize = new Size(imageDimension.getWidth(), imageDimension.getHeight());
		
		window = new JFrame("PCRobot Window");
		horizontalPanel = new JPanel();
		horizontalPanel.setLayout(new BoxLayout(horizontalPanel, BoxLayout.X_AXIS));
		label = new JLabel[numOfImages];
		icon = new ImageIcon[numOfImages];
		currentImage = new Mat[numOfImages];
		
		for (int i = 0; i < numOfImages; i++) {
		    try {
		        icon[i] = new ImageIcon();
				label[i] = new JLabel(icon[i]);
				label[i].setPreferredSize(imageDimension);
				horizontalPanel.add(label[i]);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
		
		debugging = new JLabel();
		debugging.setFont(new Font("Consolas", 0, 26));
		debugging.setPreferredSize(new Dimension(515, 0));
		debugging.setText("<empty>");
		
		horizontalPanel.add(debugging);
		window.getContentPane().add(horizontalPanel);
		window.pack();
		
        window.setVisible(true);
        window.addWindowListener(new AppCloser());
        window.setResizable(false);
	}
	
	public void setImage (int i, Mat img) {
		currentImage[i] = img;
		Imgproc.resize(img, img, imageSize);
	    MatOfByte matOfByte = new MatOfByte();
	    Imgcodecs.imencode(".jpg", img, matOfByte);
	    byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;
	    try {
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);
	        icon[i].setImage(bufImage);
	    } catch (Exception e) {
	    }
	}
	
	public void setImage (int i, BufferedImage img) {
	    try {
	        icon[i].setImage(img);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void update () {
		window.repaint();
		window.pack();
	}

	public void setText(String text) {
		debugging.setText(text);
	}
}
