/**
 * 
 */
package chord;

import java.math.BigInteger;

import chord.peer_info.PeerInfo;
import communication.Client;
import utils.Log;
import utils.Utils;

public class FixFingerTable implements Runnable {

	private ChordController chord;

	@Override
	public void run() {

	}

	static String getKeyToLookUp(String id, int i) {

		return "";
	}

	public FixFingerTable(ChordController chord) {
		this.chord = chord;
	}
}
