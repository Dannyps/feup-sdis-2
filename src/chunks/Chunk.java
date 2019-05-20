package chunks;

public class Chunk {

	protected String senderID;
	protected String fileID;
	protected int chunkNo;

	public Chunk(String senderID, String fileID, int chunkNo) {
		this.senderID = senderID;
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}
}
