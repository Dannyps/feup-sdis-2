/**
 * 
 */
package chunks;

import communication.Client;
import data.BackupRequest;

public class GetChunk extends Chunk implements Runnable {

	private BackupRequest backupRequest;

	public GetChunk(BackupRequest backupRequest, int chunkNo) {
		super("", "", chunkNo);

	}

	@Override
	public void run() {

	}

}
