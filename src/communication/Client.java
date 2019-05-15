package communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import utils.Utils;

public class Client {
	private static final short SOCKET_TIMEOUT = 5000;
	private static final char DELIMITER_1 = '\t';
	private static final char DELIMITER_2 = '\f';
	private static final String SUPPORTED_CIPHERS = "TLS_DHE_RSA_WITH_AES_128_CBC_SHA";
	private static final ArrayList<String> cipher = new ArrayList<String>(Arrays.asList(SUPPORTED_CIPHERS));

	public static String sendMessage(InetAddress addr, int port, String message, boolean waitForResponse) {

		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

		SSLSocket socket = createSocket(socketFactory, addr, port);
		if (socket == null)
			return null;

		socket.setEnabledCipherSuites(cipher.toArray(new String[0]));

		if (!sendMessage(message, socket))
			return null;

		String response = null;
		if (waitForResponse)
			response = getResponse(socket);

		if (!closeSocket(socket))
			return null;

		return response;
	}

	private static SSLSocket createSocket(SSLSocketFactory socketFactory, InetAddress addr, int port) {
		try {
			SSLSocket socket = (SSLSocket) socketFactory.createSocket(addr, port);
			socket.setSoTimeout(SOCKET_TIMEOUT);
			return socket;
		} catch (IOException e) {
			System.err.println("Connection refused - couldn't connect to server");
			return null;
		}
	}

	private static boolean sendMessage(String message, SSLSocket socket) {
		try {
			send(message, socket);
		} catch (IOException e1) {
			System.err.println("Connection refused - couldn't send message");
			return false;
		}
		return true;
	}

	/**
	 * Write to the socket (send message)
	 * 
	 * @throws IOException
	 */
	public static void send(String message, SSLSocket socket) throws IOException {
		OutputStream sendStream = socket.getOutputStream();
		sendStream.write(encode(message.getBytes(StandardCharsets.ISO_8859_1)));
		sendStream.write(DELIMITER_1);
	}

	private static boolean closeSocket(SSLSocket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			System.err.println("Error closing connection");
			return false;
		}
		return true;
	}

	private static byte[] encode(byte[] sendData) {
		LinkedList<Byte> tmp = new LinkedList<Byte>();
		for (byte data : sendData) {
			if (data == DELIMITER_2) {
				tmp.add((byte) DELIMITER_2);
				tmp.add((byte) DELIMITER_2);
			} else if (data == DELIMITER_1) {
				tmp.add((byte) DELIMITER_2);
				tmp.add((byte) DELIMITER_1);
			} else {
				tmp.add(data);
			}
		}

		return Utils.convert(tmp);
	}

	public static String getResponse(SSLSocket socket) {
		InputStream readStream = getSocketInputStream(socket);
		if (readStream == null)
			return null;

		byte[] readData = readFromStream(readStream, 1024 + Utils.MAX_LENGTH_CHUNK);
		if (readData == null)
			return null;

		if (!closeSocket(socket))
			return null;

		return new String(readData, StandardCharsets.ISO_8859_1);
	}

	private static InputStream getSocketInputStream(Socket socket) {
		try {
			return socket.getInputStream();
		} catch (IOException e) {
			System.err.println("Error getting input stream");
			return null;
		}
	}

	private static byte[] readFromStream(InputStream readStream, int size) {
		byte[] readData = new byte[size];
		try {
			readStream.read(readData);
		} catch (SocketTimeoutException e) {
			System.err.println("Socket timeout");
			return null;
		} catch (IOException e) {
			System.err.println("Error reading");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return readData;
	}

}
