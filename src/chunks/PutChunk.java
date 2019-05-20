package chunks;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Arrays;
import communication.Client;

public class PutChunk extends Chunk implements Runnable {

	private byte[] body;
	private int replicationDeg;

	public PutChunk(String fileID, int chunkNo, int replicationDeg, byte[] body) {
		super(null, fileID, chunkNo);
	}

	@Override
	public void run() {
		File file = new File("x.pdf");

		try {
			Client.sendMessage(InetAddress.getByName("localhost"), 1234, "TESTE", false);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

}
