package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import chord.Node;
import utils.AddrPort;
import utils.PrintMessage;

public class Init {

	public static void main(String[] argv) throws UnknownHostException, InterruptedException, ExecutionException {
		PrintMessage.printMessages = true;
		PrintMessage.printWarnings = true;
		// an alternative to System.out.
		PrintMessage.i("Info", "Initiating... ");
		InetSocketAddress mySocket = null;

		if (argv.length == 0) {
			// No m was provided
			PrintMessage.i("Info", "Please provide a port. Usage: java service.Init <port> [peer_ip:peer_port]");
			System.exit(1);
		} else {
			mySocket = new InetSocketAddress(getOwnAddress(), Integer.parseInt(argv[0]));
			if (argv.length == 1) {
				// I am the first node. I must begin chord.
				PrintMessage.i("Info", "Creating a chord network.");

				PrintMessage.i("Info", "Peers may connect to this chord network by using "
						+ mySocket.getAddress().toString().substring(1) + ":" + mySocket.getPort() + " as peer.");
				Node n = new Node(mySocket);
				new RMIListen(n);
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
					@Override
					public void run() {
						n.writeData();
					}
				}));
			} else {
				// I got an IP address. I must connect to it and join their chord.
				PrintMessage.i("Info", "Joining the specified chord network.");
				try {
					AddrPort peer = new AddrPort(argv[1]);
					Node n = new Node(mySocket, peer.getInetSocketAddress());
					new RMIListen(n);
					Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
						@Override
						public void run() {
							n.writeData();
						}
					}));
				} catch (Exception e) {
					PrintMessage.e("FATAL", "The passed address is not valid.");
					e.printStackTrace();
					System.exit(2);
				}
			}
		}

	}

	/**
	 * Gets its Own Address with the given port.
	 *
	 * @return this peer's address
	 */
	private static InetAddress getOwnAddress() {
		InetAddress mcastaddr;
		try {
			mcastaddr = InetAddress.getByName("225.0.0.0");
			MulticastSocket multicastSocket = new MulticastSocket(15432);
			multicastSocket.joinGroup(mcastaddr);
			multicastSocket.send(new DatagramPacket(new byte[1], 1, mcastaddr, 15432));

			DatagramPacket packet = new DatagramPacket(new byte[1], 1);

			try {
				multicastSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			multicastSocket.close();
			return packet.getAddress();
		} catch (Exception e1) {
			PrintMessage.e("FATAL", "Couldn't get own address!");
			e1.printStackTrace();
			System.exit(-3);
			return null;
		}
	}
}
