package service;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Init {
	public static void main(String[] argv) {

		// an alternative to System.out.
		PrintStream info = new PrintStream(System.out);

		if (argv.length == 0) {
			// No m was provided
			info.println("Please provide an m for chord. Usage: java Chord <m>");
			System.exit(1);
		} else if (argv.length == 1) {
			// I am the first node. I must begin chord.
			info.println("Creating a chord network with m = " + argv[0] + ".");
		} else {
			// I got an IP address. I must connect to it and join their chord.
			InetAddress toConnect = null;
			try {
				toConnect = InetAddress.getByName(argv[1]);
			} catch (UnknownHostException e) {
				info.println("The passed address is not valid.");
				System.exit(2);
				e.printStackTrace();
			}
			info.println(toConnect.getAddress());
		}
	}
}
