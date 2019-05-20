package service;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import chord.Node;
import utils.AddrPort;

public class Init {

	private static final int PORT = 7654;

	public static void main(String[] argv) throws UnknownHostException, InterruptedException, ExecutionException {

		// an alternative to System.out.
		PrintStream info = new PrintStream(System.out);

		InetSocketAddress mySocket = null;

		Node me = null;
		if (argv.length == 0) {
			// No m was provided
			info.println("Please provide a port. Usage: java service.Init <port> [peer_ip:peer_port]");
			System.exit(1);
		} else if (argv.length == 1) {
			// I am the first node. I must begin chord.
			info.println("Creating a chord network.");
			mySocket = new InetSocketAddress(getOwnAddress(), Integer.parseInt(argv[0]));
			info.println("Peers may connect to this chord network by using "
					+ mySocket.getAddress().toString().substring(1) + ":" + mySocket.getPort() + " as peer.");
			me = new Node(mySocket);
		} else {
			// I got an IP address. I must connect to it and join their chord.
			try {
				AddrPort peer = new AddrPort(argv[1]);
				me = new Node(mySocket, peer.getInetSocketAddress());
			} catch (Exception e) {
				info.println("The passed address is not valid.");
				System.exit(2);
				e.printStackTrace();
			}
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
