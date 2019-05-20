package data.info;

public class ChunkInfo extends Info {

	private static final String SEPARATOR = "_";

	private Integer actualRepDegree;
	private Integer size;
	private Integer chunkId;

	public ChunkInfo(Integer chunkId, String fileId, Integer size) {
		super(fileId);
		this.chunkId = chunkId;
		this.size = size;
	}

	public ChunkInfo(Integer chunkId, String fileId) {
		super(fileId);
		this.chunkId = chunkId;
	}

	public Integer getChunkId() {
		return chunkId;
	}

	public Integer getActualRepDegree() {
		return actualRepDegree;
	}

	public void setActualRepDegree(Integer actualRepDegree) {
		this.actualRepDegree = actualRepDegree;
	}

	public String getFilename() {
		StringBuilder s = new StringBuilder(fileId);
		s.append(SEPARATOR);
		s.append(chunkId);
		return s.toString();
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getSize() {
		return size;
	}

}
