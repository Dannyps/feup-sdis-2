package controller;

import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;

public class Controller {
	private static final String PEER_DIRECTORY = "peer";
	private static final String BACKUP_DIRECTORY = "backup";
	private static final String CHUNK_DIRECTORY = "chk";
	private static final String STATE_DIRECTORY = "state";
	private static final String DIRECTORY_DELIMETER = "/";

	private static Controller instance = new Controller();

	private static ControllerChunk chunkController;
	private static ControllerBackup backupController;
	private static ControllerRestore restoreController;

	private short peerAccessPoint;
	private String peer_version;

	private ServerSocket tcpSocket;

	private static Set<String> deletedSent;

	private int tcpPort;
	private boolean allowSaving;
	private String localIP;

	private Controller() {
	}

	public static Controller getInstance() {
		return instance;
	}

	public void init() {
		restoreController = new ControllerRestore();
		backupController = new ControllerBackup();
		chunkController = new ControllerChunk();
	}

	public String getBackupFilePath(String fileID, String chunkNo) {
		StringBuilder s = new StringBuilder();
		s.append(PEER_DIRECTORY);
		s.append(peerAccessPoint);
		s.append(DIRECTORY_DELIMETER);
		s.append(BACKUP_DIRECTORY);
		s.append(DIRECTORY_DELIMETER);
		s.append(fileID);
		try {
			Files.createDirectories(Paths.get(s.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		s.append(DIRECTORY_DELIMETER);
		s.append(CHUNK_DIRECTORY);
		s.append(chunkNo);

		return s.toString();
	}

	private String getPeerStatePath() {

		StringBuilder s = new StringBuilder();
		s.append(PEER_DIRECTORY);
		s.append(peerAccessPoint);
		s.append(DIRECTORY_DELIMETER);
		s.append(STATE_DIRECTORY);
		try {
			Files.createDirectories(Paths.get(s.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		s.append(DIRECTORY_DELIMETER);
		s.append(peerAccessPoint);
		return s.toString();
	}

//	public void saveState() {
//		try {
//			saveState(getPeerStatePath());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void restorePreviousState() {
//		String path = getPeerStatePath();
//		try {
//			readObjects(path);
//			System.out.println("FOUND PEER SERIALIZED STATE!!!");
//		} catch (ClassNotFoundException | IOException e) {
//			try {
//				resetState(path);
//			} catch (IOException e1) {
//			}
//			System.out.println("NO PEER SERIALIZED STATE FOUND. STARTING FROM SCRATCH");
//		}
//	}

//	private void saveState(String dirPath) throws FileNotFoundException, IOException {
//
//		ObjectOutputStream outStr = new ObjectOutputStream(new FileOutputStream(new File(dirPath)));
//		outStr.writeObject(restoreController);
//		outStr.writeObject(backupController);
//		outStr.writeObject(chunkController);
//		outStr.writeObject(chunkRegister);
//		outStr.writeObject(putchunkRegister);
//		outStr.writeObject(deletedSent);
//		outStr.close();
//	}
//
//	@SuppressWarnings("unchecked")
//	private void readObjects(String dirPath) throws ClassNotFoundException, IOException {
//		InputStream inputStream = new BufferedInputStream(new FileInputStream(dirPath));
//		ObjectInput objectStream = new ObjectInputStream(inputStream);
//		restoreController = (ControllerRestore) objectStream.readObject();
//		backupController = (ControllerBackup) objectStream.readObject();
//		chunkController = (ControllerChunk) objectStream.readObject();
//		chunkRegister = (MessageLog) objectStream.readObject();
//		putchunkRegister = (MessageLog) objectStream.readObject();
//		deletedSent = (Set<String>) objectStream.readObject();
//		objectStream.close();
//	}
//
//	private void resetState(String dirPath) throws IOException {
//		restoreController = new ControllerRestore();
//		backupController = new ControllerBackup();
//		chunkController = new ControllerChunk();
//		chunkRegister = new MessageLog();
//		putchunkRegister = new MessageLog();
//		deletedSent = new TreeSet<String>();
//	}

	public Set<String> getDeletedSent() {
		return deletedSent;
	}

	public synchronized void registerDelete(String fileId) {
		deletedSent.add(fileId);
	}

	public synchronized void unregisterDelete(String fileId) {
		deletedSent.remove(fileId);
	}

	public ControllerChunk getChunkController() {
		return chunkController;
	}

	public ControllerBackup getBackupController() {
		return backupController;
	}

	public ControllerRestore getRestoredController() {
		return restoreController;
	}

//	public MessageLog getPutchunkRegister() {
//		return putchunkRegister;
//	}
//
//	public MessageLog getChunkRegister() {
//		return chunkRegister;
//	}

	public boolean getAllowSaving() {
		return allowSaving;
	}

	public void setAllowSaving(boolean allow) {
		this.allowSaving = allow;
	}

	public ControllerRestore getRestoredManager() {
		return restoreController;
	}

	public ServerSocket getTCPSocket() {
		return this.tcpSocket;
	}

	public final String getLocalIP() {
		return localIP;
	}

	public final short getAccessPoint() {
		return peerAccessPoint;
	}

	public final String getVersion() {
		return peer_version;
	}

}
