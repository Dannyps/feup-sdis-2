package main;

import java.net.InetAddress;
import java.net.UnknownHostException;

import chord.ChordController;
import communication.Server;
import peer.Peer;
import utils.SingletonThreadPoolExecutor;

public class Main {

	private static Integer localPort;
	private static InetAddress firstPeerAddress;
	private static Integer firstPeerPort;
	private static ChordController chordController;
	private static Server server;
	private static Peer peer;

	public static void main(String[] args) {
		if (!verifyArguments(args))
			return;

		if (!parseArguments(args))
			return;

		chordController = new ChordController(localPort);

		server = initiateServer(localPort);
		if (server == null)
			return;

		peer = new Peer(chordController, server, generatePeerID());

		peer.joinNetwork(firstPeerAddress, firstPeerPort);

		new ReadInput(peer).run();

		closeSession();
	}

	private static boolean verifyArguments(String[] args) {
		if (args.length < 1) {
			System.err.println("Error: Need a port Number");
			return false;
		}
		return true;
	}

	private static boolean parseArguments(String[] args) {
		localPort = Integer.valueOf(args[0]);
		if (args.length >= 3) {
			try {
				firstPeerAddress = InetAddress.getByName(args[1]);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return false;
			}
			firstPeerPort = Integer.valueOf(args[2]);
		}
		return true;
	}

	private static String generatePeerID() {
		StringBuilder s = new StringBuilder("Your ID: ");
		s.append(chordController.getPeerInfo().getId());
		System.out.println(s.toString());
		return chordController.getPeerInfo().getId();
	}

	private static Server initiateServer(Integer port) {
		try {
			return new Server(new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" }, port);
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
	}

	private static void closeSession() {
		server.closeConnection();
		SingletonThreadPoolExecutor.getInstance().get().shutdownNow();
		System.out.println("Program Terminated");
	}

}
