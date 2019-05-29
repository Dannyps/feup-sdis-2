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
import java.util.List;
import javax.net.ssl.SSLSocket;
import chunks.Chunk;
import controller.Controller;
import peer.Peer;
import utils.Confidentiality;
import utils.Log;
import utils.Utils;

public class MessageHandler implements Runnable {

	private static final String DELIMITER = "\r\n";
	private SSLSocket socket;
	/*** private String myPeerID; ***/
	private Peer peer;
	private byte[] readData;

	private int predecessorPort;
	private String predecessorAddress;
	private int successorPort;
	private String successorAddress;

	public MessageHandler(Peer peer, byte[] readData, SSLSocket socket, int predecessorPort, String predecessorAddress,
			int successorPort, String successorAddress) {
		super();
		this.peer = peer;
		this.readData = readData;
		this.socket = socket;
		/*** this.myPeerID = peer.getchordController().getPeerInfo().getId(); ***/
		this.predecessorPort = predecessorPort;
		this.predecessorAddress = predecessorAddress;
		this.successorPort = successorPort;
		this.successorAddress = successorAddress;
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

		String[][] lines = parseMessageContent(request);

		try {
			return executeMessage(lines[0], lines[1], lines[2][0], request);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return new String("");
		}
	}

	private String[][] parseMessageContent(String request) {
		String[] lines = request.split(DELIMITER);

		String[] firstLine = lines[0].split(" "), secondLine = new String[] {};
		String thirdLine = "";

		if (lines.length > 2) {
			thirdLine = request.substring(request.indexOf(DELIMITER + DELIMITER) + 4, request.length());
		}

		if (lines.length > 1) {
			secondLine = lines[1].split(" ");
		}

		return new String[][] { firstLine, secondLine, new String[] { thirdLine } };
	}

	private String executeMessage(String[] firstLine, String[] secondLine, String thirdLine, String request)
			throws UnknownHostException {
		switch (MessageType.valueOf(firstLine[0])) {
		case PUTCHUNK:
			parsePutChunkMsg(secondLine, thirdLine);
			return null;
		case KEEPCHUNK:
			parseKeepChunkMsg(secondLine, thirdLine);
			return null;
		case STORED:
			return parseStoredMsg(secondLine);
		case INITDELETE:
			parseInitDelete(firstLine, secondLine);
			return null;
		case DELETE:
			parseDelete(secondLine);
			return null;
		case GETCHUNK:
			return parseGetChunkMsg(secondLine);
		case CHUNK:
			return parseChunkMsg(secondLine, thirdLine);
		default:
			StringBuilder s = new StringBuilder("Unexpected message received: ");
			s.append(request);
			Log.LOGGER.warning(s.toString());
			break;
		}
		return request;
	}

	private void parseKeepChunkMsg(String[] header, String body) throws UnknownHostException {

		InetAddress addr = createInetAddress(header[1]);

		String fileID = header[3];
		int chunkNo = Integer.parseInt(header[4]);

		StringBuilder s = new StringBuilder(fileID);
		s.append("_");
		s.append(chunkNo);

		/*** Path filePath = Peer.getPath().resolve(s.toString()); ***/

		String senderID = header[0].trim();
		int port = Integer.parseInt(header[2].trim());
		int replicationDegree = Integer.parseInt(header[5]);

		byte[] body_bytes = body.getBytes(StandardCharsets.ISO_8859_1);
		String encryption_key = header[6];
		String fileName = header[7];

		if (String.valueOf(Controller.getInstance().getAccessPoint()) == senderID) {// a mensagem ja deu uma volta
																					// completa. repDeg nao vai ser o
			// desejado
			// enviar STORE para o predecessor
			Log.LOGGER.info("KeepChunk: I am responsible ");
//			System.out.println("keepchunk 8080 - clientMessage1");
			Client.sendMessage(/*** chordController.getPredecessor().getAddr() ***/
					InetAddress.getByName(predecessorAddress), /*** chordController.getPredecessor().getPort() ***/
					predecessorPort, MessageFactory.getStored(/*** myPeerID ***/
							"", fileID, chunkNo, 0),
					false);
			return;
		}

		Controller c = Controller.getInstance();
//		if (!myPeerID.equals(senderID)) {

		if (c.getBackupController().hasFile(fileID))
			return;

		if (!c.getChunkController().registerChunk(chunkNo, 0, replicationDegree, fileID, body_bytes, encryption_key,
				fileName))
			return;

		if (!Peer.capacityExceeded(body_bytes.length)) { // tem espaco para fazer o backup
			Log.LOGGER.info("Writing/Saving chunk");
			c.getChunkController().saveChunk(chunkNo, fileID);

			c.getChunkController().incrementReplicationCount(chunkNo, fileID);

			if (replicationDegree != 1) {
//				 enivar KEEPCHUNK para o sucessor
//				System.out.println("keepchunk 8080 - clientMessage2");
				Client.sendMessage(/*** chordController.getSuccessor(0).getAddr() **/
						InetAddress.getByName(successorAddress), /*** chordController.getSuccessor(0).getPort() ***/
						successorPort, MessageFactory.getKeepChunk(senderID, addr, port, fileID, chunkNo,
								replicationDegree - 1, body_bytes, encryption_key, fileName),
						false);

			} else {// sou o ultimo a guardar
				// enviar STORE ao predecessor
//				System.out.println("keepchunk 8080 - clientMessage3");
				Client.sendMessage(/*** chordController.getPredecessor().getAddr() ***/
						addr, /*** chordController.getPredecessor().getPort() ***/
						predecessorPort, MessageFactory.getStored(/*** chordController.getPeerInfo().getId() **/
								"", fileID, chunkNo, 1),
						false);
				return;
			}
		} else {
			// enviar KEEPCHUNK para o seu sucessor
//			System.out.println("keepchunk 8080 - clientMessage4");
			Client.sendMessage(/*** chordController.getSuccessor(0).getAddr() **/
					InetAddress.getByName(successorAddress), /*** chordController.getSuccessor(0).getPort() ***/
					successorPort, MessageFactory.getKeepChunk(senderID, addr, port, fileID, chunkNo, replicationDegree,
							body_bytes, encryption_key, fileName),
					false);
			Log.LOGGER.warning("Capacity Exceeded");
		}
//		} else {// I AM ASKING FOR THE BACKUP sou dono do ficheiro
////		reencaminhar a mensagem para o proximo
//
//			c.getBackupController().increment(senderID, chunkNo);
////			PeerInfo nextPeer = chordController.getSuccessor(0);
////			DBUtils.setIamStoring(dbConnection, fileInfo.getFileId(), false);
//			Client.sendMessage(/*** chordController.getSuccessor(0).getAddr() ***/
//					InetAddress.getByName(successorAddress), /*** chordController.getSuccessor(0).getPort() ***/
//					successorPort, MessageFactory.getKeepChunk(senderID, addr, port, fileID, chunkNo, replicationDegree,
//							body_bytes, encryption_key, fileName),
//					false);
//		}

	}

	private String parseChunkMsg(String[] secondLine, String body) {

		Confidentiality c = new Confidentiality(secondLine[2]);

		byte[] body_bytes = c.decrypt(body.getBytes(StandardCharsets.ISO_8859_1));

		System.out.println(new String(body_bytes, StandardCharsets.ISO_8859_1));
		StringBuilder s = new StringBuilder("restoreFile-");
		s.append(secondLine[3]);

		Path filepath = Peer.getPath().resolve(s.toString());
		createFile(filepath);

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

	private void parseInitDelete(String[] firstLine, String[] secondLine) throws UnknownHostException {
		String fileToDelete = secondLine[0];
		Controller c = Controller.getInstance();
		int repDegree = c.getChunkController().getChunk(0, "").getDesiredRepDegree(); /*** colocar o ID */
		deleteFile(fileToDelete, repDegree);
	}

	private String parseStoredMsg(String[] secondLine) throws UnknownHostException {
		/** confirmar ordem dos argumentos **/
		String fileID = secondLine[0];
		Integer chunkNo = Integer.valueOf(secondLine[1]);
		Integer repDegree = Integer.valueOf(secondLine[2]);

		Controller c = Controller.getInstance();

		// initiator peer -> register the stored message
		if (c.getBackupController().increment(fileID, chunkNo)) {
			System.out.println("Successfully saved file");
			return null;
		}

		// not initiator peer -> increases rep. count of the stored chunk, if has one
		c.getChunkController().incrementReplicationCount(chunkNo, fileID);

		Client.sendMessage(/*** chordController.getPredecessor().getAddr() ***/
				InetAddress.getByName(predecessorAddress), /*** chordController.getPredecessor().getPort() ***/
				predecessorPort, MessageFactory.getStored(/*** myPeerID ***/
						"", fileID, chunkNo, repDegree),
				false);

		return null;
	}

	private void parseDelete(String[] secondLine) throws NumberFormatException, UnknownHostException {
		deleteFile(secondLine[0].trim(), Integer.parseInt(secondLine[1]));
	}

	private void deleteFile(String fileToDelete, int repDegree) throws UnknownHostException {
		StringBuilder s = new StringBuilder("Received Delete for file: ");
		s.append(fileToDelete);
		s.append(". Rep Degree: ");
		s.append(repDegree);
		System.out.println(s.toString());

		Controller c = Controller.getInstance();
		List<Integer> removed = c.getChunkController().deleteFileChunks(fileToDelete);

		if (!removed.isEmpty()) {
			s.setLength(0);
			s.append("deleted - ");
			s.append(removed.size());
			s.append(" chunks of file -> ");
			s.append(fileToDelete);
			System.out.println(s.toString());

			repDegree--;
		}

		if (repDegree > 0 || removed.isEmpty()) {
			s.setLength(0);
			s.append("Forwarding delete to peer: ");
//			s.append(peer.getchordController().getSuccessor(0).getId());

			System.out.println(s.toString());
			Client.sendMessage(/*** peer.getchordController().getSuccessor(0).getAddr() ***/
					InetAddress.getByName("localhost"),
					/*** peer.getchordController().getSuccessor(0).getPort() ***/
					successorPort, MessageFactory.getDelete(/*** myPeerID ***/
							"", fileToDelete, repDegree),
					false);

			s.setLength(0);
			s.append("Forwarded delete: ");
			s.append(fileToDelete);
			Log.LOGGER.info(s.toString());
		}

	}

	private void parsePutChunkMsg(String[] header, String body) throws UnknownHostException {

		InetAddress addr = createInetAddress(header[1]);

		String fileID = header[3];
		int chunkNo = Integer.parseInt(header[4]);

		StringBuilder s = new StringBuilder(fileID);
		s.append("_");
		s.append(chunkNo);

		/*** Path filePath = Peer.getPath().resolve(s.toString()); ***/

		String senderID = header[0].trim();
		int port = Integer.parseInt(header[2].trim());
		int replicationDegree = Integer.parseInt(header[5]);

		byte[] body_bytes = body.getBytes(StandardCharsets.ISO_8859_1);
		String encryption_key = header[6];
		String fileName = header[7];

		Controller c = Controller.getInstance();
		/*** if (!myPeerID.equals(senderID)) { ***/

		if (c.getBackupController().hasFile(fileID))
			return;

		if (!c.getChunkController().registerChunk(chunkNo, 0, replicationDegree, fileID, body_bytes, encryption_key,
				fileName))
			return;

		if (!Peer.capacityExceeded(body_bytes.length)) { // tem espaco para fazer o backup
			Log.LOGGER.info("Writing/Saving chunk");
			c.getChunkController().saveChunk(chunkNo, fileID);

			c.getChunkController().incrementReplicationCount(chunkNo, fileID);

			if (replicationDegree != 1) {
//				 enivar KEEPCHUNK para o sucessor
//				System.out.println("putchunk 8080 - clientMessage1");
				Client.sendMessage(/*** chordController.getSuccessor(0).getAddr() **/
						InetAddress.getByName(successorAddress), /*** chordController.getSuccessor(0).getPort() ***/
						successorPort, MessageFactory.getKeepChunk(senderID, addr, port, fileID, chunkNo,
								replicationDegree - 1, body_bytes, encryption_key, fileName),
						false);

			} else {// sou o ultimo a guardar
				// enviar STORE ao que pediu o backup
//				System.out.println("putchunk 8080 - clientMessage2");
				Client.sendMessage(addr, port, MessageFactory.getStored(/*** chordController.getPeerInfo().getId() ***/
						"", fileID, chunkNo, 1), false);
				return;
			}
		} else {
			// enviar KEEPCHUNK para o seu sucessor
//			System.out.println("putchunk 8080 - clientMessage3");
			Client.sendMessage(/*** chordController.getSuccessor(0).getAddr() **/
					InetAddress.getByName(successorAddress), /*** chordController.getSuccessor(0).getPort() ***/
					successorPort, MessageFactory.getKeepChunk(senderID, addr, port, fileID, chunkNo, replicationDegree,
							body_bytes, encryption_key, fileName),
					false);
			Log.LOGGER.warning("Capacity Exceeded");
		}
//		} else {// sou o dono do ficheiro que quero fazer backup...
//			// nao faz senido guardarmos um ficheiro com o chunk, visto que guardamos o
//			// ficheiro
//			// enviar o KEEPCHUNK
//
//		c.getBackupController().increment(senderID, chunkNo);
////			PeerInfo nextPeer = chordController.getSuccessor(0);
////			DBUtils.setIamStoring(dbConnection, fileInfo.getFileId(), false);
//		Client.sendMessage(/*** chordController.getSuccessor(0).getAddr() ***/
//				InetAddress.getByName(successorAddress), /*** chordController.getSuccessor(0).getPort() ***/
//				successorPort, MessageFactory.getKeepChunk(senderID, addr, port, fileID, chunkNo, replicationDegree, body_bytes,
//						encryption_key, fileName),
//				false);
//		}
	}

	private String parseGetChunkMsg(String[] secondLine) throws UnknownHostException {

		InetAddress addr = createInetAddress(secondLine[0]);
		if (addr == null)
			return null;

		Integer chunkNo = Integer.valueOf(secondLine[3]);
		Integer port = Integer.valueOf(secondLine[1]);
		String fileID = secondLine[2];

		Controller c = Controller.getInstance();
		Chunk chk = c.getChunkController().getChunk(chunkNo, fileID);

		if (chk == null) {
			Client.sendMessage(/*** this.peer.getchordController().getSuccessor(0).getAddr() ***/
					InetAddress.getByName(successorAddress), /***
																 * this.peer.getchordController().getSuccessor(0).getPort()
																 ***/
					successorPort, MessageFactory.getGetChunk(/*** this.myPeerID ***/
							"", addr, port, fileID, chunkNo),
					false);
		} else {
			String body = new String(chk.retrieveData(), StandardCharsets.ISO_8859_1);
			System.out.println("chunkNo-> " + chunkNo);
			System.out.println(new String(new Confidentiality(chk.getEncryptKey()).decrypt(chk.retrieveData())));
			System.out.println("\n\n");
			Client.sendMessage(addr, port, MessageFactory.getChunk(/*** this.myPeerID ***/
					"", fileID, chunkNo, body.getBytes(StandardCharsets.ISO_8859_1), chk.getEncryptKey(),
					chk.getFileName()), false);
		}

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
			return true;
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
