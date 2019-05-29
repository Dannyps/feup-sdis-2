package peer;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import chunks.DeleteChunk;
import chunks.GetChunk;
import chunks.PutChunk;
import communication.Server;
import controller.Controller;
import utils.Confidentiality;
import utils.Log;
import utils.SingletonThreadPoolExecutor;
import utils.Utils;

public class Peer {

//	public static final int MAX_CHUNK_SIZE = 16;
	private static final int STORAGE_CAPACITY = Integer.MAX_VALUE;
	private static int STORAGE_USED = 0;
	private static final String ENCRYPTION_ALGORITHM = "md5";
//	private static final String PEER_BASE_PATH = "peer_";
	private static Path path;
	private static String id;
	private Server server;

	public Peer(/*** ChordManager chordManager, ***/
	Server server) {
		/*** this.chordManager = chordManager; ***/
		this.server = server;
		this.server.setPeer(this);
		this.id = generatePeerID();
		Controller.getInstance().init(id);
		generatePath(id);
	}

	/**
	 * Creates (if necessary) the directory where the chunks are stored
	 * 
	 * @param id
	 */
	private static void generatePath(String id) {
		String peerPath = Controller.getInstance().getPeerFilePath(id);
		Peer.setPath(Paths.get(peerPath));
	}

	private static String generatePeerID() {
		/***
		 * StringBuilder s = new StringBuilder("Your ID: ");
		 * s.append(chordController.getPeerInfo().getId());
		 * System.out.println(s.toString()); return
		 * chordController.getPeerInfo().getId();
		 ***/
		return "ID_TODO";
	}

	public void joinNetwork(InetAddress addr, Integer port) {
		if (addr != null) {
			/*** chordController.join(addr, port); ***/
		}
		executeThreadPool(SingletonThreadPoolExecutor.getInstance().get());
	}

	private void executeThreadPool(ScheduledThreadPoolExecutor s) {
		s.execute(server);
		/*** s.execute(chordManager); ***/
	}

	/**
	 * Generate a file ID
	 * 
	 * @param filename - the filename
	 * @return Hexadecimal md5 encoded fileID
	 * @throws IOException, NoSuchAlgorithmException
	 */
	public String getFileID(String filename) throws IOException, NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
		byte[] hash = digest.digest(generateFileIdentifier(filename).getBytes(StandardCharsets.UTF_8));
		/*** return Utils.getIdFromHash(hash, ChordManager.getM() / 8); ***/
		return Utils.getIdFromHash(hash, 0 / 8);
	}

	private String generateFileIdentifier(String filename) throws IOException {
		BasicFileAttributes attr = Files.readAttributes(Paths.get(filename), BasicFileAttributes.class);
		StringBuilder s = new StringBuilder(filename);
		s.append(attr.lastModifiedTime());
		return s.toString();
	}

	public static Path getPath() {
		return path;
	}

	public static void setPath(Path p) {
		Peer.path = p;
	}

	/**
	 * Returns false if has space to store the chunk.
	 * 
	 */
	public static boolean capacityExceeded(int amount) {
		if (STORAGE_CAPACITY > STORAGE_USED + amount) {
			STORAGE_USED += amount;
			return false;
		} else {
			return true;
		}
	}

	public static void decreaseStorageUsed(int amount) {
		STORAGE_USED -= amount;
	}

	/*** */
	public void backup(String filename, Short degree, String encryptKey, String fileName) {
		StringBuilder s = new StringBuilder(filename);
		String fileID;
		s.append(" -> ");
		try {
			fileID = getFileID(filename);
			s.append(getFileID(filename));
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			return;
		}
		Log.LOGGER.severe(s.toString());

		Confidentiality c = (encryptKey == null) ? new Confidentiality() : new Confidentiality(encryptKey);

		byte[] file = Utils.readFile(filename).getBytes(StandardCharsets.ISO_8859_1);
		backupChunks(file, fileID, c, degree, fileName);
	}

	private void backupChunks(byte[] file, String fileID, Confidentiality c, Short degree, String fileName) {
		int chunkNo = 0;
		byte[] body;

		String encryptKey = new String(c.getKey(), StandardCharsets.ISO_8859_1);
		int chunksNo = file.length / Utils.MAX_LENGTH_CHUNK + 1;
		Controller.getInstance().getBackupController().addFile(chunksNo, degree, fileID);

		while ((chunkNo + 1) * Utils.MAX_LENGTH_CHUNK <= file.length) {
			body = c.encript(
					Arrays.copyOfRange(file, chunkNo * Utils.MAX_LENGTH_CHUNK, (chunkNo + 1) * Utils.MAX_LENGTH_CHUNK));
			SingletonThreadPoolExecutor.getInstance().get().execute(
					new PutChunk(fileID, chunkNo, degree, body, encryptKey, fileName/*** , this.getChordManager() ***/
					));
			chunkNo++;
		}

		body = c.encript(Arrays.copyOfRange(file, chunkNo * Utils.MAX_LENGTH_CHUNK, file.length));
		SingletonThreadPoolExecutor.getInstance().get().execute(
				new PutChunk(fileID, chunkNo, degree, body, encryptKey, fileName/*** , this.getChordManager() ***/
				));
	}

	public void delete(String fileID) {
		SingletonThreadPoolExecutor.getInstance().get().execute(new DeleteChunk(fileID/*** , this.getChordManager() ***/
		));
	}

	public void restore(String fileID) {
		int chunkNo = Controller.getInstance().getBackupController().getNumberOfChunks(fileID);
		Controller.getInstance().getRestoredController().addFile(chunkNo, fileID);
		for (int i = 0; i < chunkNo; i++) {
			SingletonThreadPoolExecutor.getInstance().get()
					.execute(new GetChunk(fileID, i/*** , this.getChordManager() ***/
					));
		}
	}

}
