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

	public static final int MAX_CHUNK_SIZE = 64000;
	private static final int STORAGE_CAPACITY = Integer.MAX_VALUE;
	private static int STORAGE_USED = 0;
	private static final String ENCRYPTION_ALGORITHM = "md5";
	private static final String PEER_BASE_PATH = "peer_";
	private static Path path;

	private Server server;

	public Peer(Server server, String id) {
		this.server = server;
		this.server.setPeer(this);
		Controller.getInstance().init();
		generatePath(id);
	}

	public void joinNetwork(InetAddress addr, Integer port) {
		if (addr != null) {
//			chordController.join(addr, port);
		}
		executeThreadPool(SingletonThreadPoolExecutor.getInstance().get());
	}

	private void executeThreadPool(ScheduledThreadPoolExecutor s) {
		s.execute(server);
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
	public void backup(String filename, Short degree, String encryptKey) {
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
//		BackupRequest backupRequest = new BackupRequest(fileID, filename,
//				new String(c.getKey(), StandardCharsets.ISO_8859_1), degree,
//				Math.floorDiv(file.length, CHUNK_SIZE) + 1);
		backupChunks(file, fileID, c, degree, encryptKey);
	}

	private void backupChunks(byte[] file, String fileID, Confidentiality c, Short degree, String encryptKey) {
		int chunkNo = 0;
		byte[] body;

		while ((chunkNo + 1) * MAX_CHUNK_SIZE <= file.length) {
			body = c.encript(Arrays.copyOfRange(file, chunkNo * MAX_CHUNK_SIZE, (chunkNo + 1) * MAX_CHUNK_SIZE));
			SingletonThreadPoolExecutor.getInstance().get()
					.execute(new PutChunk(fileID, chunkNo, degree, body, encryptKey));
			chunkNo++;
		}

		Controller.getInstance().getBackupController().addFile(chunkNo + 1, degree, fileID);
		body = c.encript(Arrays.copyOfRange(file, chunkNo * MAX_CHUNK_SIZE, file.length));
		SingletonThreadPoolExecutor.getInstance().get()
				.execute(new PutChunk(fileID, chunkNo, degree, body, encryptKey));
	}

//	public void delete(String fileID) {
//		Controller.getInstance().registerDelete(fileID);
//		SingletonThreadPoolExecutor.getInstance().get().execute(new DeleteChunk(fileID));
//	}
//
//	public void restore(BackupRequest backupRequest) {
//		Controller.getInstance().getRestoredController().addFile(backupRequest.getNumberOfChunks(),
//				backupRequest.getFileId());
//
//		for (int i = 0; i < backupRequest.getNumberOfChunks(); i++) {
//			SingletonThreadPoolExecutor.getInstance().get().execute(new GetChunk(backupRequest, i));
//		}
//	}

	/**
	 * When a peer joins, tell him which files he is responsible for.
	 */
	public void sendResponsability() {
		// tem de ser feito
	}

	/**
	 * Creates (if necessary) the directory where the chunks are stored
	 * 
	 * @param id
	 */
	private static void generatePath(String id) {
		StringBuilder s = new StringBuilder(PEER_BASE_PATH);
		s.append(id);

		Peer.setPath(Paths.get(s.toString()));

		if (!Files.exists(Peer.getPath())) {
			try {
				Files.createDirectory(Peer.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (Files.exists(Peer.getPath())) {
			return;
		}

		try {
			Files.createDirectory(Peer.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
