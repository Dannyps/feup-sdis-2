package chord;

import chord.peer_info.AbstractPeerInfo;
import chord.peer_info.NullPeerInfo;
import communication.Client;
import utils.Log;

public class CheckPredecessor implements Runnable {

	private ChordController chordController;

	CheckPredecessor(ChordController chordController) {
		this.chordController = chordController;
	}

	@Override
	public void run() {

	}

}
