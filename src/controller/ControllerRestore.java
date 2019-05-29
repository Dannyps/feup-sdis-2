package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import peer.Peer;
import utils.Utils;

import java.io.Serializable;

public class ControllerRestore implements Serializable {

	private static final long serialVersionUID = -1707120929515118568L;
	private volatile ConcurrentHashMap<String, ArrayList<ArrayList<Byte>>> files;

	public ControllerRestore() {
		files = new ConcurrentHashMap<String, ArrayList<ArrayList<Byte>>>();
	}

	public synchronized void addFile(int numberOfChunks, String fileId) {
		if (files == null)
			files = new ConcurrentHashMap<String, ArrayList<ArrayList<Byte>>>();

		ArrayList<ArrayList<Byte>> file = new ArrayList<ArrayList<Byte>>();
		fillFile(file, numberOfChunks);

		files.put(fileId, file);
	}

	private synchronized void fillFile(ArrayList<ArrayList<Byte>> file, int numberOfChunks) {
		for (int i = 0; i < numberOfChunks; i++)
			file.add(null);
	}

	private synchronized void fillDataArray(byte[] data, ArrayList<Byte> dataList) {
		for (byte d : data)
			dataList.add(d);
	}

	public synchronized boolean saveChunk(int chunkNumber, String fileId, byte[] data) {
		ArrayList<ArrayList<Byte>> file = files.get(fileId);

		if (verifyConditions(file, chunkNumber))
			return false;

		if (chunkNumber == file.size() - 1 && data == null) {
			file.remove(chunkNumber);
			return true;
		}

		ArrayList<Byte> dataList = new ArrayList<Byte>();
		fillDataArray(data, dataList);

		setChunk(file, dataList, chunkNumber, fileId);
		return true;
	}

	private void setChunk(ArrayList<ArrayList<Byte>> file, ArrayList<Byte> dataList, int chunkNumber, String fileId) {
		file.set(chunkNumber, dataList);
		files.put(fileId, file);
	}

	private boolean verifyConditions(ArrayList<ArrayList<Byte>> file, int chunkNumber) {
		if (file == null)
			return true;

		if (file.get(chunkNumber) != null)
			return true;

		return false;
	}

	public synchronized byte[] reassemble(String fileId) {
		if (!isWhole(fileId))
			return null;

		List<ArrayList<Byte>> file = files.get(fileId);
		byte[] reassembling = new byte[2 * Utils.MAX_LENGTH_CHUNK * file.size()];

		int idx = 0;
		for (List<Byte> chunk : file) {
			for (Byte b : chunk) {
				reassembling[idx] = b;
				idx++;
			}
		}

		return Arrays.copyOfRange(reassembling, 0, idx);
	}

	public boolean isWhole(String fileId) {
		List<ArrayList<Byte>> file = files.get(fileId);
		if (file == null)
			return false;

		for (ArrayList<Byte> chunk : file) {
			if (chunk == null)
				return false;

		}
		return true;
	}

}
