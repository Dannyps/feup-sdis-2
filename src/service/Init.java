package service;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import chord.Node;
import message.Message;
import message.MessageType;
import rmi.RmiInterface;
import utils.AddrPort;
import utils.PrintMessage;

public class Init implements RmiInterface {

	public static void main(String[] argv) throws UnknownHostException, InterruptedException, ExecutionException {
		PrintMessage.printMessages = true;
		PrintMessage.printWarnings = true;
		// an alternative to System.out.
		PrintStream info = new PrintStream(System.out);
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
				new Node(mySocket);
			} else {
				// I got an IP address. I must connect to it and join their chord.
				PrintMessage.i("Info", "Joining the specified chord network.");
				try {
					AddrPort peer = new AddrPort(argv[1]);
					new Node(mySocket, peer.getInetSocketAddress());
				} catch (Exception e) {
					info.println("The passed address is not valid.");
					e.printStackTrace();
					System.exit(2);
				}
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

	@Override
	public void backup() {
		File file = new File("teste.txt");

		try {
			byte[] fileContent = Files.readAllBytes(file.toPath());

			Message message = new Message(MessageType.FILE_BACKUP, fileContent);


			InetSocketAddress mySocket = new InetSocketAddress(getOwnAddress(), Integer.parseInt("1234"));

			AddrPort addrPort = new AddrPort("localhost", 1234);

			AddrPort peer = new AddrPort("1234");
			Node node = new Node(mySocket, peer.getInetSocketAddress());

			Message response = node.write(addrPort.getInetSocketAddress(), message, true);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
