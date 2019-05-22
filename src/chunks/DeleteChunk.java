package chunks;

import java.net.InetAddress;
import java.net.UnknownHostException;

import communication.Client;
import communication.MessageFactory;
import utils.Log;

public class DeleteChunk extends Chunk implements Runnable {

	public DeleteChunk(String fileId) {
		super(null, fileId, -1);
	}

	@Override
	public void run() {
		StringBuilder s = new StringBuilder("Sending Delete request for file: ");
		s.append(fileID);

		Log.LOGGER.info(s.toString());
//		PeerInfo successor = chordController.getChunkOwner(fileID);
//		Client.sendMessage(successor.getAddr(), successor.getPort(), MessageFactory.getInitDelete(senderID, fileID),
//				false);

		try {
			Client.sendMessage(InetAddress.getByName("localhost"), 8081, MessageFactory.getInitDelete(senderID, fileID),
					false);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		s.setLength(0);
		s.append("Sent Delete request for file: ");
		s.append(fileID);

	}

}
