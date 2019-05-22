package controller;

import java.io.Serializable;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerBackup implements Serializable {

	private static final long serialVersionUID = -1871731576486115174L;
	private ConcurrentHashMap<String, Integer> desiredRepDegrees;
	private ConcurrentHashMap<String, LinkedList<Integer>> filesChunks;

	public ControllerBackup() {
		desiredRepDegrees = new ConcurrentHashMap<String, Integer>();
		filesChunks = new ConcurrentHashMap<String, LinkedList<Integer>>();
	}

	public synchronized void addFile(int numberOfChunks, int desiredRepDegree, String fileId) {
		createFileCnt(fileId, numberOfChunks);
		desiredRepDegrees.put(fileId, desiredRepDegree);
	}

	private synchronized void createFileCnt(String fileId, int numberOfChunks) {
		LinkedList<Integer> arr = new LinkedList<Integer>();
		for (int i = 0; i < numberOfChunks; i++) {
			arr.add(0);
		}

		filesChunks.put(fileId, arr);
	}

	public boolean hasFile(String fileId) {
		return this.filesChunks.get(fileId) != null;
	}

	private synchronized boolean variation(String fileId, int chunkNo, int variation) {
		LinkedList<Integer> chunks = filesChunks.get(fileId);
		if (chunks != null) {
			chunks.set(chunkNo, (chunks.get(chunkNo) + variation));
			return true;
		} else
			return false;
	}

	public synchronized boolean increment(String fileId, int chunkNo) {
		return variation(fileId, chunkNo, 1);
	}

	public synchronized boolean decrement(String fileId, int chunkNo) {
		return variation(fileId, chunkNo, -1);
	}

	public synchronized List<Integer> getChunksBelowDegree(String fileId, int degree) {
		List<Integer> res = new LinkedList<Integer>();
		List<Integer> repDegs = filesChunks.get(fileId);

		int i = 0;
		for (int repDeg : repDegs) {
			if (repDeg < degree)
				res.add(i);
			i++;
		}
		return res;
	}

	public synchronized String getState() {
		StringBuilder s = new StringBuilder();
		s.append("INITIATOR PEER INFORMATION\n\n");

		if (filesChunks.isEmpty())
			s.append("This peer never initiated any backup\n");
		else {
			s.append("file identifier | desired replication degree | real replication degree of each chunk\n\n");
			for (Map.Entry<String, LinkedList<Integer>> entry : filesChunks.entrySet()) {
				String fileId = entry.getKey();

				s.append(fileId);
				s.append(" | ");
				s.append(desiredRepDegrees.get(fileId));
				s.append(" | ");
				s.append(entry.getValue());
				s.append("\n");
			}
		}

		s.append("\n");

		return s.toString();
	}
}
