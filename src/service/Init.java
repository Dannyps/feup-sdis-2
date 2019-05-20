package service;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import chord.Node;

public class Init {

	private static final int PORT = 7654;

	public static void main(String[] argv) throws UnknownHostException, InterruptedException, ExecutionException {
		
		
		// an alternative to System.out.
		PrintStream info = new PrintStream(System.out);
		
		InetSocketAddress mySocket = new InetSocketAddress(getOwnAddress(), PORT);
		info.println(mySocket);
		
		
		Node me = null;
		if (argv.length == 0) {
			// No m was provided
			info.println("Please provide an m for chord. Usage: java Chord <m>");
			System.exit(1);
		} else if (argv.length == 1) {
			// I am the first node. I must begin chord.
			info.println("Creating a chord network with m = " + argv[0] + ".");
			me = new Node(Integer.parseInt(argv[0]), mySocket);
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

			InetSocketAddress PeerSocket = new InetSocketAddress(toConnect, PORT);
			me = new Node(Integer.parseInt(argv[0]), mySocket, PeerSocket);

		}
	}

	/**
	 * Gets his Own Address with the given port.
	 *
	 * @return this peer's address
	 */
	private static InetAddress getOwnAddress() {
		InetAddress mcastaddr;
		try {
			mcastaddr = InetAddress.getByName("225.0.0.0");
			MulticastSocket multicastSocket = new MulticastSocket(8546);
			multicastSocket.joinGroup(mcastaddr);
			multicastSocket.send(new DatagramPacket(new byte[1], 1, mcastaddr, 8546));

			DatagramPacket packet = new DatagramPacket(new byte[1], 1);

			try {
				multicastSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			multicastSocket.close();
			return packet.getAddress();
		} catch (Exception e1) {
			System.err.print("Couldn't get own address!");
			System.exit(-3);
			return null;
		}
	}
}
