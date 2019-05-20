package data.info;

public class FileStoredInfo extends Info {

	private String peerRequesting;
	private Integer desiredRepDegree;
	private Boolean iAmResponsible;

	public FileStoredInfo(String fileId, Boolean iAmResponsible, Integer desiredRepDegree) {
		super(fileId);
		this.iAmResponsible = iAmResponsible;
		this.desiredRepDegree = desiredRepDegree;
	}

	public FileStoredInfo(String fileId, Boolean iAmResponsible) {
		super(fileId);
		this.iAmResponsible = iAmResponsible;
	}

	public Boolean getiAmResponsible() {
		return iAmResponsible;
	}

	public String getPeerRequesting() {
		return peerRequesting;
	}

	public void setPeerRequesting(String peerId) {
		this.peerRequesting = peerId;
	}

	public void setDesiredRepDegree(int desiredRepDegree) {
		this.desiredRepDegree = desiredRepDegree;
	}

	public Integer getDesiredRepDegree() {
		return this.desiredRepDegree;
	}
}
