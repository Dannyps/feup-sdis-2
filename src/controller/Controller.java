package controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;

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

	private String peerAccessPoint;
	private String peer_version;

	private boolean allowSaving;
	private String localIP;

	private Controller() {
	}

	public static Controller getInstance() {
		return instance;
	}

	public void init(String peerAccessPoint) {
		this.peerAccessPoint = peerAccessPoint;

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

	public String getPeerFilePath(String fileID) {
		StringBuilder s = new StringBuilder();
		s.append(PEER_DIRECTORY);
		s.append(peerAccessPoint);
		try {
			Files.createDirectories(Paths.get(s.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return s.toString();
	}

	/***
	 * private String getPeerStatePath() {
	 * 
	 * StringBuilder s = new StringBuilder(); s.append(PEER_DIRECTORY);
	 * s.append(peerAccessPoint); s.append(DIRECTORY_DELIMETER);
	 * s.append(STATE_DIRECTORY); try {
	 * Files.createDirectories(Paths.get(s.toString())); } catch (IOException e) {
	 * e.printStackTrace(); }
	 * 
	 * s.append(DIRECTORY_DELIMETER); s.append(peerAccessPoint); return
	 * s.toString(); }
	 * 
	 * public void saveState() { try { saveState(getPeerStatePath()); } catch
	 * (IOException e) { e.printStackTrace(); } }
	 * 
	 * private void restorePreviousState() { String path = getPeerStatePath(); try {
	 * readObjects(path); System.out.println("FOUND PEER SERIALIZED STATE!!!"); }
	 * catch (ClassNotFoundException | IOException e) { try { resetState(path); }
	 * catch (IOException e1) { } System.out.println("NO PEER SERIALIZED STATE
	 * FOUND. STARTING FROM SCRATCH"); } }
	 * 
	 * private void saveState(String dirPath) throws FileNotFoundException,
	 * IOException {
	 * 
	 * ObjectOutputStream outStr = new ObjectOutputStream(new FileOutputStream(new
	 * File(dirPath))); outStr.writeObject(restoreController);
	 * outStr.writeObject(backupController); outStr.writeObject(chunkController);
	 * outStr.close(); }
	 * 
	 * @SuppressWarnings("unchecked") private void readObjects(String dirPath)
	 * throws ClassNotFoundException, IOException { InputStream inputStream = new
	 * BufferedInputStream(new FileInputStream(dirPath)); ObjectInput objectStream =
	 * new ObjectInputStream(inputStream); restoreController = (ControllerRestore)
	 * objectStream.readObject(); backupController = (ControllerBackup)
	 * objectStream.readObject(); chunkController = (ControllerChunk)
	 * objectStream.readObject(); objectStream.close(); }
	 * 
	 * private void resetState(String dirPath) throws IOException {
	 * restoreController = new ControllerRestore(); backupController = new
	 * ControllerBackup(); chunkController = new ControllerChunk(); }
	 ***/
	public ControllerChunk getChunkController() {
		return chunkController;
	}

	public ControllerBackup getBackupController() {
		return backupController;
	}

	public ControllerRestore getRestoredController() {
		return restoreController;
	}

	public boolean getAllowSaving() {
		return allowSaving;
	}

	public void setAllowSaving(boolean allow) {
		this.allowSaving = allow;
	}

	public ControllerRestore getRestoredManager() {
		return restoreController;
	}

	public final String getLocalIP() {
		return localIP;
	}

	public final String getAccessPoint() {
		return peerAccessPoint;
	}

	public final String getVersion() {
		return peer_version;
	}

}
