/**
 * 
 */
package chunks;

import java.net.InetAddress;
import java.net.UnknownHostException;
import communication.Client;
import communication.MessageFactory;

public class GetChunk extends Chunk implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8285476267121797798L;

	public GetChunk(String fileID, int chunkNo/*** , ChordManager chord ***/
	) {
		super("", fileID, chunkNo);
		/*** this.chord = chord; ***/
	}

	@Override
	public void run() {

		StringBuilder s = new StringBuilder("Sending GetChunk request for file: ");
		s.append(fileID);

		/***
		 * String getChunkMessage =
		 * MessageFactory.getGetChunk(chord.getPeerInfo().getId(),
		 * chord.getPeerInfo().getAddr(), chord.getPeerInfo().getPort(),
		 * this.backupRequest.getFileId(), this.chunkNo);
		 * 
		 * PeerInfo owner = chord.getChunkOwner(fileID);
		 * Client.sendMessage(owner.getAddr(), owner.getPort(), getChunkMessage, false);
		 ***/
		try {
			Client.sendMessage(/*** owner.getAddr() ***/
					InetAddress.getByName("localhost"), /*** owner.getPort() ***/
					8080, MessageFactory.getGetChunk(/*** chord.getPeerInfo().getId() ***/
							"", /*** chord.getPeerInfo().getAddr() ***/
							InetAddress.getByName("localhost"),
							/*** chord.getPeerInfo().getPort() ***/
							8080, fileID, chunkNo),
					false);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		s.setLength(0);
		s.append("Sent GetChunk request for file: ");
		s.append(fileID);
	}

}