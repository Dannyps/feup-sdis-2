/**
 * 
 */
package chord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import chord.peer_info.AbstractPeerInfo;
import chord.peer_info.NullPeerInfo;
import chord.peer_info.PeerInfo;
import communication.Client;
import utils.Log;
import utils.SingletonThreadPoolExecutor;
import utils.Utils;

public class ChordController implements Runnable {

	private static final int M = 32; // 32bits - 4 bytes
	private static final String ENCRYPTION_ALGORITHM = "md5";
	private static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.MILLISECONDS;

	private static final short THREAD_STABILIZE_INITIAL_DELAY = 10;
	private static final short THREAD_STABILIZE_PERIOD = 2500;
	private static final short THREAD_CHECK_PREDECESSOR_INITIAL_DELAY = 1000;
	private static final short THREAD_CHECK_PREDECESSOR_PERIOD = 10000;
	private static final short THREAD_FIX_FINGER_TABLE_INITIAL_DELAY = 2000;
	private static final short THREAD_FIX_FINGER_TABLE_PERIOD = 5000;

	private PeerInfo peerInfo;
	private ArrayList<PeerInfo> fingerTable;
	private AbstractPeerInfo predecessor;

	private Deque<PeerInfo> nextPeers; // this is an enhancement to the protocol

	public ChordController(Integer port) {
		nextPeers = new ConcurrentLinkedDeque<PeerInfo>();
		predecessor = new NullPeerInfo();

		this.fingerTable = new ArrayList<PeerInfo>();
		InetAddress addr = createAddress();
		if (addr == null)
			return;

		MessageDigest digest = createDigest();
		if (digest == null)
			return;

		String addr_string = String.valueOf(addr);
		String port_string = String.valueOf(port);
		StringBuilder s = new StringBuilder();
		s.append(addr_string);
		s.append(port_string);

		this.setPeerInfo(new PeerInfo(
				Utils.getIdFromHash(digest.digest(s.toString().getBytes(StandardCharsets.ISO_8859_1)), M / 8), addr,
				port));

		addPeersToFingerTable(getM());

		setNextPeer(getPeerInfo());
	}

	private InetAddress createAddress() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private MessageDigest createDigest() {
		try {
			return MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void addPeersToFingerTable(int m) {
		for (int i = 0; i < m; i++) {
			getFingerTable().add(getPeerInfo());
		}
	}

	private void setNextPeer(PeerInfo nextPeer) {
		if (nextPeer == null || nextPeer.isNull())
			return;
		this.getFingerTable().set(0, nextPeer);
		PeerInfo first = this.nextPeers.peek();
		if (first != null) {
			if (first.getId().equals(nextPeer.getId()))
				return;
		}
		nextPeers.push(nextPeer);
	}

	@Override
	public void run() {

		ScheduledThreadPoolExecutor thread = SingletonThreadPoolExecutor.getInstance().get();

		thread.scheduleAtFixedRate(new Stabilize(this), THREAD_STABILIZE_INITIAL_DELAY, THREAD_STABILIZE_PERIOD,
				DEFAULT_TIMEUNIT);

		thread.scheduleAtFixedRate(new CheckPredecessor(this), THREAD_CHECK_PREDECESSOR_INITIAL_DELAY,
				THREAD_CHECK_PREDECESSOR_PERIOD, DEFAULT_TIMEUNIT);

		thread.scheduleAtFixedRate(new FixFingerTable(this), THREAD_FIX_FINGER_TABLE_INITIAL_DELAY,
				THREAD_FIX_FINGER_TABLE_PERIOD, DEFAULT_TIMEUNIT);
	}

	public void join(InetAddress addr, int port) {

	}

	/**
	 * Returna o successor da key, ou a quem perguntor
	 * 
	 * @param key a procurar
	 * @return
	 */
	public String lookup(String key) {
		return "";
	}

	/**
	 * 
	 * @param x
	 * @return True if successor was updated
	 */
	public void stabilize(AbstractPeerInfo x) {

	}

	/**
	 * Notify newly found closer successor node that I might be its predecessor
	 * 
	 * @param newSuccessorId Closer successor than previous successor.
	 */
	public void notify(PeerInfo newSuccessor) {

	}

	public PeerInfo getChunkOwner(String key) {
		return new PeerInfo(key); // tem de ser feito
	}

	public List<PeerInfo> getNextPeers() {
		List<PeerInfo> nextPeersArray = new LinkedList<PeerInfo>();
		return nextPeersArray; // tem de ser feito
	}

	public void popNextPeer() {

	}

	public PeerInfo getNextPeer() {
		return nextPeers.peek();
	}

	/**
	 * @return the m
	 */
	public static int getM() {
		return M;
	}

	public PeerInfo getSuccessor(int index) {
		return this.fingerTable.get(index);
	}

	public AbstractPeerInfo getPredecessor() {
		return this.predecessor;
	}

	public void setPredecessor(AbstractPeerInfo nullPeerInfo) {
		this.predecessor = nullPeerInfo;
	}

	/**
	 * @return the peerInfo
	 */
	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	/**
	 * @param peerInfo the peerInfo to set
	 */
	public void setPeerInfo(PeerInfo peerInfo) {
		this.peerInfo = peerInfo;
	}

	/**
	 * @return the fingerTable
	 */
	public ArrayList<PeerInfo> getFingerTable() {
		return fingerTable;
	}

	public void updateNextPeers(Deque<PeerInfo> peersReceived) {
		PeerInfo nextPeer = nextPeers.pop();
		nextPeers.clear();
		peersReceived.push(nextPeer);
		nextPeers = peersReceived;
	}
}
