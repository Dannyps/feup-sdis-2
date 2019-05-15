package chunks;

import java.util.Arrays;
import communication.Client;
import chord.ChordController;
import chord.peer_info.PeerInfo;

public class PutChunk extends Chunk implements Runnable {

	private ChordController chord;
	private byte[] body;
	private int replicationDeg;

	public PutChunk(String fileID, int chunkNo, int replicationDeg, byte[] body, ChordController chord) {
		super(chord.getPeerInfo().getId(), fileID, chunkNo);
		this.replicationDeg = replicationDeg;
		this.body = Arrays.copyOf(body, body.length);
		this.chord = chord;
	}

	@Override
	public void run() {
		StringBuilder s = new StringBuilder("Sending PutChunk request for file: ");
		s.append(fileID);

		PeerInfo owner = chord.getChunkOwner(fileID);
		String putChunkMessage = ""; // putchunk message
		Client.sendMessage(owner.getAddr(), owner.getPort(), putChunkMessage, false);

		s.setLength(0);
		s.append("Sent PutChunk request for file: ");
		s.append(fileID);
	}

}
