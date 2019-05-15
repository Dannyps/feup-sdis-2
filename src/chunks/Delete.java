package chunks;

import chord.ChordController;
import chord.peer_info.PeerInfo;
import communication.Client;
import utils.Log;

public class Delete extends Chunk implements Runnable {

	private ChordController chordController;

	public Delete(String fileId, ChordController chordController) {
		super(chordController.getPeerInfo().getId(), fileId, -1);
		this.chordController = chordController;
	}

	@Override
	public void run() {
		StringBuilder s = new StringBuilder("Sending Delete request for file: ");
		s.append(fileID);

		Log.LOGGER.info(s.toString());
		PeerInfo successor = chordController.getChunkOwner(fileID);

		String message = "";// intiate delete message
		Client.sendMessage(successor.getAddr(), successor.getPort(), message, false);

		s.setLength(0);
		s.append("Sent Delete request for file: ");
		s.append(fileID);
	}

}
