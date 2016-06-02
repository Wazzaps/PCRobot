package com.gmail.secondlifedvi.PCRobotController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TCPClient {
	private static TCPClient[] instances = new TCPClient[255];
	private static int instanceCount = 0;
	private Socket robotSocket;
	private OutputStream os;
	private OutputStreamWriter osw;
	private BufferedWriter bw;
	private InputStream is;
	private InputStreamReader isr;
	private BufferedReader br;
	public String host;
	private int port = 25000;

	public void open(Robot robot) {
		try {
			System.out.println("Attempting to connect to robot at " + host + ":" + port);
			try {
				//InetAddress address = InetAddress.getByName(host);
				robotSocket = new Socket(host, port);

				// The message sender
				os = robotSocket.getOutputStream();
				osw = new OutputStreamWriter(os);
				bw = new BufferedWriter(osw);

				// The message receiver
				is = robotSocket.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);

				new Thread("InputThread") {
					public void run() {
						while (robotSocket.isConnected()) {
							try {
								String msg = br.readLine();
								if (msg != null && msg != "") {
									String[] message = msg.split(":");
									switch (message[0].toLowerCase()) {
									case "analoginput":
										
										if (message[1].toLowerCase().equals("val")) {
											robot.analogInputs.get(toInt(message[2])).value = toDouble(message[3]);
										}
										break;
									}
								}
							} catch (SocketException e) {
								// Ignore
							} catch (IOException e) {
								e.printStackTrace();
							} catch (Exception e) {
								System.err.println(e);
							}
						}
					}
				}.start();
			} catch (UnknownHostException e) {
				System.err.println("UnknownHostException: Make sure you are connected to the robot");
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public boolean isConnected() {
		return robotSocket.isConnected();
	}

	private void init(Robot robot, String host, int port) {
		this.host = host;
		this.port = port;
		instances[instanceCount] = this;
		instanceCount++;
		open(robot);
	}

	public TCPClient(Robot robot, int teamNumber) {
		init(robot, "roborio-" + teamNumber + "-frc.local", this.port);
	}

	public TCPClient(Robot robot, int teamNumber, int port) {
		init(robot, "roborio-" + teamNumber + "-frc.local", port);
	}

	public TCPClient(Robot robot, String host) {
		init(robot, host, this.port);
	}

	public TCPClient(Robot robot, String host, int port) {
		init(robot, host, port);
	}

	public void sendRaw(String command) {
		/* Commands: (25/5/16 - 12:56)
		 * 
		 * -> CANTalon:New:(int id):(int port):(boolean inverted) 
		 * -> CANTalon:Set:(int id):(double speed)
		 * 
		 * -> Victor:New:(int id):(int port):(boolean inverted)
		 * -> Victor:Set:(int id):(double speed)
		 * 
		 * -> Compressor:(int pcmPort)
		 * 
		 * -> Solenoid:New:(int id):(int pcmPort):(int port) 
		 * -> Solenoid:Set:(int id):(boolean on)
		 * 
		 * -> Relay:New:(int id):(int port) 
		 * -> Relay:Set:(int id):(boolean on)
		 * 
		 * -> AnalogInput:New:(int id):(int port)
		 * <- AnalogInput:Val:(int id):(double value)
		 */
		if (robotSocket == null || robotSocket.isClosed() || !robotSocket.isConnected()) {
			return;
		}
		try {
			
			bw.write(command + "\n");
			bw.flush();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void close() {
		if (robotSocket != null) {
			try {
				robotSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void cleanup() {
		for (int i = 0; i < instances.length; i++) {
			if (instances[i] != null) {
				instances[i].close();
			}
		}
	}
	
	private int toInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private Double toDouble(String s) {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	@SuppressWarnings("unused")
	private boolean toBoolean(String s) {
		return s.toLowerCase().equals("true") || s.equals("1") || s.equals("on") || s.equals("yes");
	}
}