/**
 * 
 */
package communication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.net.ssl.SSLSocket;

import data.BackupRequest;
import peer.Peer;
import utils.Confidentiality;
import utils.Log;
import utils.Utils;

public class MessageHandler implements Runnable {

	private SSLSocket socket;
	private String myPeerID;
	private Peer peer;
	private byte[] readData;

	public MessageHandler(Peer peer, byte[] readData, SSLSocket socket) {
		super();

		this.peer = peer;
		this.readData = readData;
		this.socket = socket;
//		ChordController c = peer.getChordManager();
//		this.myPeerID = c.getPeerInfo().getId();
	}

	@Override
	public void run() {
		String response = parseMessage(readData);
		if (response != null) {
			sendResponse(socket, response);
		}
	}

	/*** */
	/**
	 * Parses the received request, processes it and returns the protocol response
	 * 
	 * @param readData
	 * @return
	 */
	public String parseMessage(byte[] readData) {
		String request = new String(readData, StandardCharsets.ISO_8859_1).trim();
		StringBuilder s = new StringBuilder("SSLServer: ");
		s.append(request);
		Log.LOGGER.finest(s.toString());

		String[] firstLine = null, secondLine = null;
		String thirdLine = null;

		return executeMessage(firstLine, secondLine, thirdLine, request);
	}


	private String executeMessage(String[] firstLine, String[] secondLine, String thirdLine, String request) {
		return request;
	}

	private String parseChunkMsg(String[] secondLine, String body) {

		BackupRequest b = null;// obter backup request de um chunk previamente guardado

		Confidentiality c = new Confidentiality(b.getEncryptKey());

		byte[] body_bytes = c.decrypt(body.getBytes(StandardCharsets.ISO_8859_1));

		StringBuilder s = new StringBuilder("restoreFile-");
		s.append(b.getFilename());

		Path filepath = Peer.getPath().resolve(s.toString());
		if (!createFile(filepath))
			return null;

		AsynchronousFileChannel channel = openAsynchronousFileChannel(filepath);
		if (channel == null)
			return null;

		CompletionHandler<Integer, ByteBuffer> writter = createCompletionHandler();

		ByteBuffer src = ByteBuffer.allocate(body_bytes.length);
		src.put(body_bytes);
		src.flip();
		channel.write(src, Integer.parseInt(secondLine[1]) * Utils.MAX_LENGTH_CHUNK, src, writter);

		return null;
	}

	private CompletionHandler<Integer, ByteBuffer> createCompletionHandler() {
		return new CompletionHandler<Integer, ByteBuffer>() {
			@Override
			public void completed(Integer result, ByteBuffer buffer) {
				Log.LOGGER.info("Finished writing!");
			}

			@Override
			public void failed(Throwable arg0, ByteBuffer arg1) {
				Log.LOGGER.warning("Error: Could not write!");
			}

		};
	}

	private AsynchronousFileChannel openAsynchronousFileChannel(Path filepath) {
		try {
			return AsynchronousFileChannel.open(filepath, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private boolean createFile(Path filepath) {
		try {
			Files.createFile(filepath);
		} catch (FileAlreadyExistsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private InetAddress createInetAddress(String s) {
		try {
			return InetAddress.getByName(s);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void writeToFile(Path filePath, byte[] body_bytes) {
		try {
			Utils.writeToFile(filePath, body_bytes);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param socket
	 * @param response
	 */
	void sendResponse(SSLSocket socket, String response) {
		OutputStream sendStream;
		try {
			sendStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		byte[] sendData = response.getBytes(StandardCharsets.ISO_8859_1);
		try {
			sendStream.write(sendData);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

}
