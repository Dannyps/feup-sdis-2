package controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import chunks.Chunk;
import java.io.Serializable;

public class ControllerChunk implements Serializable {

	private static final long serialVersionUID = -6706606832954986107L;
	private Set<Chunk> chunks;

	public ControllerChunk() {
		chunks = ConcurrentHashMap.newKeySet();
	}

	public synchronized boolean registerChunk(int chunkNo, int repDegree, int desiredRepDeg, String id, byte[] data,
			String encryptKey, String fileName) {
		return chunks.add(new Chunk(id, chunkNo, repDegree, desiredRepDeg, data, encryptKey, fileName));
	}

	public synchronized Chunk getChunk(int chunkNo, String id) {
		Chunk toFind = new Chunk(id, chunkNo);
		for (Chunk c : chunks) {
			if (c.equals(toFind) && c.hashCode() == toFind.hashCode())
				return c;
		}
		return null;
	}

	public synchronized boolean saveChunk(int chunkNo, String id) {
		return getChunk(chunkNo, id) != null;
	}

	public synchronized boolean decrementReplicationCount(int chunkNo, String id) {
		Chunk chunk = getChunk(chunkNo, id);
		if (chunk != null) {
			chunk.addToActualReplication(-1);
			return chunk.getActualRepDegree() < chunk.getDesiredRepDegree();
		}

		return false;
	}

	public synchronized boolean incrementReplicationCount(int chunkNo, String id) {
		Chunk chunk = getChunk(chunkNo, id);
		if (chunk != null) {
			chunk.addToActualReplication(1);
			return chunk.getActualRepDegree() < chunk.getDesiredRepDegree();
		}

		return false;
	}

	public synchronized List<Integer> deleteFileChunks(String id) {

		List<Integer> chunkNumbers = new LinkedList<Integer>();
		for (Chunk c : chunks) {
			if (c.getId().equals(id)) {
				chunks.remove(c);
				c.delete();
				chunkNumbers.add(c.getChunkNo());
			}
		}

		return chunkNumbers;
	}

	public synchronized void deleteChunk(int chunkNo, String id) {
		Chunk chunk = getChunk(chunkNo, id);
		chunks.remove(chunk);
		chunk.delete();
	}

	public int getNumberOfChunks() {
		return this.chunks.size();
	}

	public Chunk getRandomChunk() {
		if (chunks.isEmpty())
			return null;

		return (Chunk) chunks.toArray()[new Random().nextInt(chunks.size())];
	}

	public long getTotalSizeOfChunks() {
		long count = 0;
		for (Chunk c : chunks) {
			count += c.getSize();
		}
		return count;
	}

}
