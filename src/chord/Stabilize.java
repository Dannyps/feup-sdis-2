package chord;

import chord.peer_info.AbstractPeerInfo;
import chord.peer_info.NullPeerInfo;
import chord.peer_info.PeerInfo;
import communication.Client;
import utils.Log;

public class Stabilize implements Runnable {

	private static final String DELIMITER = "\r\n";
	private ChordController chordController;

	public Stabilize(ChordController chordController) {
		this.chordController = chordController;
	}

	private AbstractPeerInfo parseResponse(String response) {
		// response should be a string of PREDECESSOR Type
		try {
			String[] args = response.trim().split(DELIMITER)[1].split(" ");
			if (args.length == 3) {
				return new PeerInfo(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new NullPeerInfo();
	}

	@Override
	public void run() {

	}
}