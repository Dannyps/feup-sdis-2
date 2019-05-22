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

		String[][] lines = parseMessageContent(request);

		return executeMessage(lines[0], lines[1], lines[2][0], request);
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

	private String executeMessage(String[] firstLine, String[] secondLine, String thirdLine, String request) {
		switch (MessageType.valueOf(firstLine[0])) {
		case PUTCHUNK:
			parsePutChunkMsg(secondLine, thirdLine);
			break;
		case STORED:
			return parseStoredMsg(secondLine);
		case INITDELETE:
			parseInitDelete(firstLine, secondLine);
			break;
		case DELETE:
			parseDelete(secondLine);
			break;
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

	private String parseChunkMsg(String[] secondLine, String body) {

		Chunk chk = Controller.getInstance().getChunkController().getChunk(/*** chunkNo */
				0, secondLine[0].trim());

		Confidentiality c = new Confidentiality(chk.getEncryptKey());

		byte[] body_bytes = c.decrypt(body.getBytes(StandardCharsets.ISO_8859_1));

		StringBuilder s = new StringBuilder("restoreFile-");
		s.append(chk.getFileName());

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

	private void parseInitDelete(String[] firstLine, String[] secondLine) {
		String fileToDelete = secondLine[0];
		Controller c = Controller.getInstance();
		int repDegree = c.getChunkController().getChunk(0, "").getDesiredRepDegree(); /*** colocar o ID */
		deleteFile(fileToDelete, repDegree);
	}

	private String parseStoredMsg(String[] secondLine) {
		InetAddress addr = createInetAddress(secondLine[0]);
		if (addr == null)
			return null;

		/** confirmar ordem dos argumentos **/
		Integer chunkNo = Integer.valueOf(secondLine[2]);
		Integer port = Integer.valueOf(secondLine[1]);
		String fileID = secondLine[2];

		Controller c = Controller.getInstance();
		// initiator peer -> register the stored message
		if (c.getBackupController().increment(fileID, chunkNo))
			return null;

		// not initiator peer -> increases rep. count of the stored chunk, if has one
		c.getChunkController().incrementReplicationCount(chunkNo, fileID);

		return null;
	}

	private void parseDelete(String[] secondLine) {
//		String fileToDelete = secondLine[0].trim();
//		if (!DBUtils.amIResponsible(dbConnection, fileToDelete))
//			deleteFile(fileToDelete, Integer.parseInt(secondLine[1]));
	}

	private void deleteFile(String fileToDelete, int repDegree) {
//		StringBuilder s = new StringBuilder("Received Delete for file: ");
//		s.append(fileToDelete);
//		s.append(". Rep Degree: ");
//		s.append(repDegree);
//		System.out.println(s.toString());
//
//		boolean isFileStored = DBUtils.isFileStored(dbConnection, fileToDelete);
//
//		if (isFileStored) {
//			for (ChunkInfo chunk : DBUtils.getAllChunksOfFile(dbConnection, fileToDelete)) {
//				Utils.deleteFile(Peer.getPath().resolve(chunk.getFilename()));
//				Peer.decreaseStorageUsed(chunk.getSize());
//			}
//
//			DBUtils.deleteFile(dbConnection, fileToDelete);
//			repDegree--;
//			s.setLength(0);
//			s.append("Deleted file: ");
//			s.append(fileToDelete);
//			Log.LOGGER.info(s.toString());
//		}
//
//		if (repDegree > 0 || !isFileStored) {
//			s.setLength(0);
//			s.append("Forwarding delete to peer: ");
//			s.append(peer.getChordManager().getSuccessor(0).getId());
//
//			System.out.println(s.toString());
//			Client.sendMessage(peer.getChordManager().getSuccessor(0).getAddr(),
//					peer.getChordManager().getSuccessor(0).getPort(),
//					MessageFactory.getDelete(myPeerID, fileToDelete, repDegree), false);
//
//			s.setLength(0);
//			s.append("Forwarded delete: ");
//			s.append(fileToDelete);
//			Log.LOGGER.info(s.toString());
//		}

	}

	private void parsePutChunkMsg(String[] header, String body) {

		InetAddress addr = createInetAddress(header[1]);

		String fileID = header[3];
		int chunkNo = Integer.parseInt(header[4]);

		StringBuilder s = new StringBuilder(fileID);
		s.append("_");
		s.append(chunkNo);

		Path filePath = Peer.getPath().resolve(s.toString());

		String senderID = header[0].trim();
		int port = Integer.parseInt(header[2].trim());
//		DBUtils.insertPeer(dbConnection, peerThatRequestedBackup);

		int replicationDegree = Integer.parseInt(header[5]);
//		DBUtils.insertStoredFile(dbConnection, fileInfo);

//		ChordController chordController = peer.getChordManager();
		byte[] body_bytes = body.getBytes(StandardCharsets.ISO_8859_1);
		String encryption_key = header[6];

//		if (!myPeerID.equals(senderID)) {

		Controller c = Controller.getInstance();
		if (c.getBackupController().hasFile(fileID))
			return;

		if (!c.getChunkController().registerChunk(chunkNo, 0, replicationDegree, fileID, body_bytes, encryption_key,
				""))
			return;

		if (!Peer.capacityExceeded(body_bytes.length)) { // tem espaco para fazer o backup
			Log.LOGGER.info("Writing/Saving chunk");
			if (!c.getChunkController().saveChunk(chunkNo, senderID))
				writeToFile(filePath, body_bytes);

			c.getChunkController().incrementReplicationCount(chunkNo, senderID);

			if (replicationDegree != 1) {
//				 enivar KEEPCHUNK para o sucessor
//					Client.sendMessage(chordController.getSuccessor(0).getAddr(),
//							chordController.getSuccessor(0).getPort(), MessageFactory.getKeepChunk(senderID, addr, port,
//									fileID, chunkNo, replicationDegree - 1, body_bytes),
//							false);

			} else {// sou o ultimo a guardar
				// enviar STORE ao que pediu o backup
//				Client.sendMessage(addr, port,
//						MessageFactory.getStored(chordController.getPeerInfo().getId(), fileID, chunkNo, 1), false);
				return;
			}
		} else {
			// enviar KEEPCHUNK para o seu sucessor
//				Client.sendMessage(chordController.getSuccessor(0).getAddr(), chordController.getSuccessor(0).getPort(),
//						MessageFactory.getKeepChunk(senderID, addr, port, fileID, chunkNo, replicationDegree, body_bytes),
//						false);
			Log.LOGGER.warning("Capacity Exceeded");
		}
//		} else {// sou o dono do ficheiro que quero fazer backup...
//			// nao faz senido guardarmos um ficheiro com o chunk, visto que guardamos o
//			// ficheiro
//			// enviar o KEEPCHUNK
//
		c.getBackupController().increment(senderID, chunkNo);
////			PeerInfo nextPeer = chordController.getSuccessor(0);
////			DBUtils.setIamStoring(dbConnection, fileInfo.getFileId(), false);
////			Client.sendMessage(nextPeer.getAddr(), nextPeer.getPort(),
////					MessageFactory.getKeepChunk(senderID, addr, port, fileID, chunkNo, replicationDegree, body_bytes), false);
//		}

	}

	private String parseGetChunkMsg(String[] secondLine) {

		InetAddress addr = createInetAddress(secondLine[0]);
		if (addr == null)
			return null;

		Integer chunkNo = Integer.valueOf(secondLine[3]);
		Integer port = Integer.valueOf(secondLine[1]);
		String fileID = secondLine[2];

//		if (!DBUtils.checkStoredChunk(dbConnection, chunkInfo)) { // I have the chunk
//			Client.sendMessage(this.peer.getChordManager().getSuccessor(0).getAddr(),
//					this.peer.getChordManager().getSuccessor(0).getPort(),
//					MessageFactory.getGetChunk(this.myPeerID, addr, port, fileID, chunkNo), false);
//			return null;
//		}

		// ReSend GETCHUNK to successor
//		String body = Utils.readFile(Peer.getPath().resolve(chunkInfo.getFilename()).toString());
//		Client.sendMessage(addr, port,
//				MessageFactory.getChunk(this.myPeerID, fileID, chunkNo, body.getBytes(StandardCharsets.ISO_8859_1)),
//				false);
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
