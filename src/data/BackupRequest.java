package data;

public class BackupRequest {

	private Short desiredRepDegree;
	private Integer numberOfChunks;
	private String filename;
	private String encryptKey;
	private String fileId;

	public BackupRequest(String fileId, String filename, Short desiredRepDegree) {
		this.fileId = fileId;
		this.filename = filename;
		this.encryptKey = null;
		this.numberOfChunks = null;
		this.desiredRepDegree = desiredRepDegree;
	}

	public BackupRequest(String fileId, String filename, String encryptKey, Short desiredRepDegree,
			Integer numberOfChunks) {
		this.fileId = fileId;
		this.filename = filename;
		this.encryptKey = encryptKey;
		this.numberOfChunks = numberOfChunks;
		this.desiredRepDegree = desiredRepDegree;
	}

	public Short getDesiredRepDegree() {
		return desiredRepDegree;
	}

	public String getFileId() {
		return fileId;
	}

	public String getFilename() {
		return filename;
	}

	public String getEncryptKey() {
		return encryptKey;
	}

	public Integer getNumberOfChunks() {
		return numberOfChunks;
	}

}
