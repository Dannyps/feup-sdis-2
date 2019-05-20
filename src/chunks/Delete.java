package chunks;

import communication.Client;
import utils.Log;

public class Delete extends Chunk implements Runnable {

	public Delete(String fileId) {
		super(null, fileId, -1);
	}

	@Override
	public void run() {

	}

}
