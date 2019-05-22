package chunks;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.concurrent.Future;

import controller.Controller;
import peer.Peer;

public class Chunk implements Comparator<Chunk>, Serializable {

	protected String senderID;
	protected String fileID;
	protected int chunkNo;
	protected int repDegree;
	protected int desiredRepDegree;
	protected byte[] data;
	private String filePath;
	private String encryptKey;
	private String fileName;

	public Chunk(String senderID, String fileID, int chunkNo) {
		this.senderID = senderID;
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	public Chunk(String fileID, int chunkNo, int repDegree, int desiredRepDegree, byte[] data, String encryptKey,
			String fileName) {
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.data = data;
		this.repDegree = repDegree;
		this.desiredRepDegree = desiredRepDegree;
		this.encryptKey = encryptKey;
		this.fileName = fileName;
		setFilePath();
	}

	private void setFilePath() {
		this.filePath = Controller.getInstance().getBackupFilePath(fileID, String.valueOf(chunkNo));
	}

	public Chunk(String id, int chunkNo) {
		this.fileID = id;
		this.chunkNo = chunkNo;
		this.data = new byte[] {};
		this.repDegree = -1;
		this.desiredRepDegree = -1;
		filePath = new String();
	}

	public void addToActualReplication(int value) {
		repDegree += value;
	}

	public final int getActualRepDegree() {
		return repDegree;
	}

	public final int getDesiredRepDegree() {
		return desiredRepDegree;
	}

	public final String getId() {
		return fileID;
	}

	public void delete() {
		new File(filePath).delete();
	}

	public final int getChunkNo() {
		return chunkNo;
	}

	public boolean store() {
		File file = new File(filePath);

		try {
			if (data == null) {
				file.createNewFile();
			} else {
				DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
				stream.close();

				AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE);

				CompletionHandler<Integer, Object> handler = new CompletionHandler<Integer, Object>() {

					@Override
					public void failed(Throwable e, Object attachment) {

						System.out.println(attachment + " failed with exception -> ");
						try {
							channel.close();
						} catch (IOException exc) {
							exc.printStackTrace();
						}
						e.printStackTrace();
					}

					@Override
					public void completed(Integer result, Object attachment) {
						try {
							channel.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				channel.write(ByteBuffer.wrap(data), 0, "Chunk saving", handler);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkNo;
		result = prime * result + ((fileID == null) ? 0 : fileID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chunk other = (Chunk) obj;
		if (chunkNo != other.chunkNo)
			return false;
		if (fileID == null) {
			if (other.fileID != null)
				return false;
		} else if (!fileID.equals(other.fileID))
			return false;
		return true;
	}

	@Override
	public int compare(Chunk c1, Chunk c2) {
		if (c1.chunkNo == c2.chunkNo) {
			return 0;
		} else if (c1.chunkNo > c2.chunkNo)
			return 1;
		else
			return -1;
	}

	public float getSize() {
		Long len = new File(filePath).length();
		return len.floatValue() / 1000f;
	}

	public byte[] retrieveData() {

		AsynchronousFileChannel channel;
		try {
			channel = AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}

		long position = 0;
		ByteBuffer buffer = ByteBuffer.allocate(Peer.MAX_CHUNK_SIZE);
		Future<Integer> operation = channel.read(buffer, position);

		while (!operation.isDone())
			;

		buffer.flip();
		byte[] data = new byte[buffer.limit()];
		buffer.get(data);
		buffer.clear();

		return data;
	}

	public final String getEncryptKey() {
		return encryptKey;
	}

	/**
	 * @return the fileName
	 */
	public final String getFileName() {
		return fileName;
	}

}
