package communication;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import peer.Peer;
import utils.Log;
import utils.SingletonThreadPoolExecutor;
import utils.Utils;

public class Server implements Runnable {

	public static final short MAX_LENGTH_PACKET = 300;
	private static final String SYSTEM_KEY_STORE = "server.keys";
	private static final String SYSTEM_KEY_STORE_PASSWORD = "123456";
	private static final String SYSTEM_TRUST_STORE = "truststore";
	private static final String SYSTEM_TRUST_STORE_PASSWORD = "123456";
	private static final short MIN_SIZE = 1024;

	private int port_number;
	private Peer peer;
	private SSLServerSocket serverSocket;

	public Server(String[] cipher_suite, int port) throws Exception {
		this.port_number = port;
		this.peer = null;
		setSystemProperties();
	}

	@Override
	public void run() {
		SSLServerSocketFactory serverFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

		if (!createServerSocket(serverFactory))
			return;

		StringBuilder s = new StringBuilder();
		s.append("Server listening on port ");
		s.append(port_number);
		Log.LOGGER.info(s.toString());

		serverSocket.setNeedClientAuth(true);
		serverSocket.setEnabledProtocols(serverSocket.getSupportedProtocols());

		loopRequests();
	}

	private boolean createServerSocket(SSLServerSocketFactory serverFactory) {
		try {
			serverSocket = (SSLServerSocket) serverFactory.createServerSocket(this.port_number);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean acceptServerSocket(SSLSocket socket) {
		try {
			socket = (SSLSocket) serverSocket.accept();
		} catch (IOException e) {
			Log.LOGGER.warning("Socket closed");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean sockerStartHandshake(SSLSocket socket) {
		try {
			socket.startHandshake();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void loopRequests() {
		while (true) {
			SSLSocket socket = null;
			if (!acceptServerSocket(socket))
				return;

			if (!sockerStartHandshake(socket))
				return;

			SingletonThreadPoolExecutor.getInstance().get()
					.execute(new MessageHandler(peer, readSocket(socket), socket));
		}
	}

	public void closeConnection() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setSystemProperties() {
		System.setProperty("javax.net.ssl.keyStore", SYSTEM_KEY_STORE);
		System.setProperty("javax.net.ssl.keyStorePassword", SYSTEM_KEY_STORE_PASSWORD);
		System.setProperty("javax.net.ssl.trustStore", SYSTEM_TRUST_STORE);
		System.setProperty("javax.net.ssl.trustStorePassword", SYSTEM_TRUST_STORE_PASSWORD);
	}

	/*** */
	public byte[] readSocket(SSLSocket socket) {
		InputStream readStream = getInputStream(socket);
		if (readStream == null)
			return null;

		/*** */
		byte[] readData = new byte[Utils.MAX_LENGTH_CHUNK + MIN_SIZE];
		try {
			int p = 0;
			byte l = 'a';
			while (l != '\t') {
				l = (byte) readStream.read();
				if (l == '\f') {
					l = (byte) readStream.read();
					readData[p] = l;
					l = 'a';
				} else {
					readData[p] = l;
				}
				p++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return readData;
	}

	private InputStream getInputStream(SSLSocket socket) {
		try {
			return socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param peer the peer to set
	 */
	public void setPeer(Peer peer) {
		this.peer = peer;
	}

}