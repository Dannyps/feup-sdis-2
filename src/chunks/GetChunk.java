/**
 * 
 */
package chunks;

import chord.ChordController;
import chord.peer_info.PeerInfo;
import communication.Client;
import data.BackupRequest;

public class GetChunk extends Chunk implements Runnable {

	private BackupRequest backupRequest;
	private ChordController chord;

	public GetChunk(BackupRequest backupRequest, int chunkNo, ChordController chord) {
		super("", "", chunkNo);
		this.backupRequest = backupRequest;
		this.chord = chord;
	}

	@Override
	public void run() {
		String id = backupRequest.getFileId();

		StringBuilder s = new StringBuilder("Sending GetChunk request for file: ");
		s.append(id);

		String getChunkMessage = "";// getchunk message
		PeerInfo owner = chord.getChunkOwner(id);
		Client.sendMessage(owner.getAddr(), owner.getPort(), getChunkMessage, false);

		s.setLength(0);
		s.append("Sent GetChunk request for file: ");
		s.append(id);
	}

}
