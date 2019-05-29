package chunks;

import java.net.InetAddress;
import java.net.UnknownHostException;
import communication.Client;
import communication.MessageFactory;

public class PutChunk extends Chunk implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2307469930219822956L;
	private byte[] body;
	private int repDegree;
	private String encryptKey;
	private String fileName;

	public PutChunk(String fileID, int chunkNo, int repDegree, byte[] body/* , ChordController chord */,
			String encryptKey, String fileName) {
		super(/*** chord.getPeerInfo().getId() ***/
				null, fileID, chunkNo);
		this.repDegree = repDegree;
		this.body = body;
		this.encryptKey = encryptKey;
		/*** this.chord = chord; ***/
		this.fileName = fileName;
	}

	@Override
	public void run() {
		/***
		 * PeerInfo owner = chord.getChunkOwner(fileID);
		 * Client.sendMessage(owner.getAddr(), owner.getPort(),
		 * MessageFactory.getPutChunk(senderID, owner.getAddr(), owner.getPort(),
		 * this.fileID, this.chunkNo, this.repDegree, this.body), false);
		 ***/
		String putChunkMessage;
		try {
			putChunkMessage = MessageFactory.getPutChunk("", InetAddress.getByName("localhost"), 8080, this.fileID,
					this.chunkNo, this.repDegree, this.body, this.encryptKey, this.fileName);

			Client.sendMessage(InetAddress.getByName("localhost"), 8081, putChunkMessage, false);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
