/**
 * 
 */
package chunks;

import communication.Client;
import communication.MessageFactory;
import data.BackupRequest;

public class GetChunk extends Chunk implements Runnable {

	public GetChunk(String fileID, int chunkNo) {
		super("", fileID, chunkNo);

	}

	@Override
	public void run() {

//		StringBuilder s = new StringBuilder("Sending GetChunk request for file: ");
//		s.append(fileID);
//
//		String getChunkMessage = MessageFactory.getGetChunk(chord.getPeerInfo().getId(), chord.getPeerInfo().getAddr(),
//				chord.getPeerInfo().getPort(), this.backupRequest.getFileId(), this.chunkNo);
//
//		PeerInfo owner = chord.getChunkOwner(fileID);
//		Client.sendMessage(owner.getAddr(), owner.getPort(), getChunkMessage, false);
//
//		s.setLength(0);
//		s.append("Sent GetChunk request for file: ");
//		s.append(fileID);
	}

}
