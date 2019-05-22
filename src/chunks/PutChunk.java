package chunks;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Arrays;
import communication.Client;
import communication.MessageFactory;

public class PutChunk extends Chunk implements Runnable {

	private byte[] body;
	private int repDegree;
//	private ChordController chord;
	private String encryptKey;

	public PutChunk(String fileID, int chunkNo, int repDegree, byte[] body/* , ChordController chord */,
			String encryptKey) {
		super(null, fileID, chunkNo);
		this.repDegree = repDegree;
		this.body = Arrays.copyOf(body, body.length);
		this.encryptKey = encryptKey;
//		this.chord = chord;
	}

	@Override
	public void run() {
//		PeerInfo owner = chord.getChunkOwner(fileID);
//		String putChunkMessage = MessageFactory.getPutChunk(senderID, owner.getAddr(), owner.getPort(), this.fileID,
//				this.chunkNo, this.repDegree, this.body);
//		Client.sendMessage(owner.getAddr(), owner.getPort(), putChunkMessage, false);

		String putChunkMessage;
		try {
			putChunkMessage = MessageFactory.getPutChunk(fileID, InetAddress.getByName("localhost"), 8081, this.fileID,
					this.chunkNo, this.repDegree, this.body, this.encryptKey);
			Client.sendMessage(InetAddress.getByName("localhost"), 8081, putChunkMessage, false);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
