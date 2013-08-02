// HTTPExtensionExample.java
// Copyright (c) MIT Media Laboratory, 2013
//
// This example Scratch helper app runs a tiny HTTP server that allows Scratch to
// play a beep sound and set and get the value of a variable named 'volume'.
//
// Inspired by Tom Lauwers Finch/Hummingbird server and Conner Hudson's Snap extensions.

import java.io.*;
import java.net.*;
import java.util.*;

public class HTTPExtensionExample {

	private static final int PORT = 12345; // set to your extension's port number
	private static int volume = 8; // replace with your extension's data, if any

	private static InputStream sockIn;
	private static OutputStream sockOut;

	private static Arduino arduino;

	public static void main(String[] args) throws IOException {
		try {
			if (args.length < 1) {
				System.err.println("Please specify serial port on command line.");
				return;
			}
			arduino = new Arduino(args[0]);
		} catch (Exception e) {
			System.err.println(e);
		}
		
		InetAddress addr = InetAddress.getLocalHost();
		System.out.println("HTTPExtensionExample helper app started on " + addr.toString());
		
		ServerSocket serverSock = new ServerSocket(PORT);
		while (true) {
			Socket sock = serverSock.accept();
			sockIn = sock.getInputStream();
			sockOut = sock.getOutputStream();
			try {
				handleRequest();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				sendResponse("unknown server error");
			}
			sock.close();
		}
	}

	private static void handleRequest() throws IOException {
		String httpBuf = "";
		int i;

		// read data until the first HTTP header line is complete (i.e. a '\n' is seen)
		while ((i = httpBuf.indexOf('\n')) < 0) {
			byte[] buf = new byte[5000];
			int bytes_read = sockIn.read(buf, 0, buf.length);
			if (bytes_read < 0) {
				System.out.println("Socket closed; no HTTP header.");
				return;
			}
			httpBuf += new String(Arrays.copyOf(buf, bytes_read));
		}
		
		String header = httpBuf.substring(0, i);
		if (header.indexOf("GET ") != 0) {
			System.out.println("This server only handles HTTP GET requests.");
			return;
		}
		i = header.indexOf("HTTP/1");
		if (i < 0) {
			System.out.println("Bad HTTP GET header.");
			return;
		}
		header = header.substring(5, i - 1);
		if (header.equals("favicon.ico")) return; // igore browser favicon.ico requests
		else if (header.equals("crossdomain.xml")) sendPolicyFile();
		else if (header.length() == 0) doHelp();
		else doCommand(header);
	}

	private static void sendPolicyFile() {
		// Send a Flash null-teriminated cross-domain policy file.
		String policyFile =
			"<cross-domain-policy>\n" +
			"  <allow-access-from domain=\"*\" to-ports=\"" + PORT + "\"/>\n" +
			"</cross-domain-policy>\n\0";
		sendResponse(policyFile);
	}

	private static void sendResponse(String s) {
		String crlf = "\r\n";
		String httpResponse = "HTTP/1.1 200 OK" + crlf;
		httpResponse += "Content-Type: text/html; charset=ISO-8859-1" + crlf;
		httpResponse += "Access-Control-Allow-Origin: *" + crlf;
		httpResponse += crlf;
		httpResponse += s + crlf;
		try {
			byte[] outBuf = httpResponse.getBytes();
			sockOut.write(outBuf, 0, outBuf.length);
		} catch (Exception ignored) { }
	}
	
	private static void doCommand(String cmdAndArgs) {
		// Essential: handle commands understood by this server
		String response = "okay";
		String[] parts = cmdAndArgs.split("/");
		String cmd = parts[0];
		
		try {
			if (cmd.equals("pinOutput")) {
				arduino.pinMode(Integer.parseInt(parts[1]), Arduino.OUTPUT);
			} else if (cmd.equals("pinInput")) {
				arduino.pinMode(Integer.parseInt(parts[1]), Arduino.INPUT);
			} else if (cmd.equals("pinHigh")) {
				arduino.digitalWrite(Integer.parseInt(parts[1]), Arduino.HIGH);
			} else if (cmd.equals("pinLow")) {
				arduino.digitalWrite(Integer.parseInt(parts[1]), Arduino.LOW);
			}
		} catch (IOException e) {
			System.err.println(e);
		}
		
		if (cmd.equals("playBeep")) {
			java.awt.Toolkit.getDefaultToolkit().beep();
		} else if (cmd.equals("volume")) {
			response = volume + "\n";
		} else if (cmd.equals("setVolume")) {
			volume = Integer.parseInt(parts[1]);
		} else if (cmd.equals("poll")) {
			// set response to a collection of sensor, value pairs, one pair per line
			// in this example there is only one sensor, "volume"
			response = "volume " + volume + "\n";
		} else {
			response = "unknown command: " + cmd;
		}
		sendResponse(response);
	}

	private static void doHelp() {
		// Optional: return a list of commands understood by this server
		String help = "HTTP Extension Example Server<br><br>";
		help += "playBeep - play the system beep<br>";
		help += "poll - return all sensor values, one sensor per line, with the sensor name and value separated by a space<br>";
		help += "setVolume/[num] - set the volume to the given number (0-10)<br>";
		help += "volume - return the current volume<br>";
		sendResponse(help);
	}

}
