package chord;

import java.net.InetSocketAddress;
import java.io.Serializable;

/**
 * AnnouncePeer
 */
public class AnnouncePeer implements Serializable {

    private static final long serialVersionUID = 49846L;

    boolean successorUpdated = false;
    boolean predecessorUpdated = false;

    public boolean mustBeForwarded() {
        return this.successorUpdated && this.predecessorUpdated;
    }

    ChordKey newPeerKey;
    InetSocketAddress newPeerAddress;

    private int hopCount = 0;

    public AnnouncePeer(ChordKey k, InetSocketAddress s) {
        this.newPeerAddress = s;
        this.newPeerKey = k;
    }

    /**
     * @return the newPeerAddress
     */
    public InetSocketAddress getNewPeerAddress() {
        return newPeerAddress;
    }

    /**
     * @return the newPeerKey
     */
    public ChordKey getNewPeerKey() {
        return newPeerKey;
    }

    public void increaseCount() {
        this.hopCount++;
    }

    /**
     * @return the hopCount
     */
    public int getHopCount() {
        return hopCount;
    }
}