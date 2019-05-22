package chord;

import java.net.InetSocketAddress;
import java.io.Serializable;

/**
 * AnnouncePeer
 */
public class AnnouncePeer implements Serializable {

    private static final long serialVersionUID = 49846L;

    private boolean successorUpdated = false;
    private boolean predecessorUpdated = false;
    private ChordKey newPeerKey = null;
    private InetSocketAddress newPeerAddress = null;

    public boolean mustBeForwarded() {
        return !(this.successorUpdated && this.predecessorUpdated);
    }

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

    /**
     * @param predecessorUpdated the predecessorUpdated to set
     */
    public void setPredecessorUpdated(boolean predecessorUpdated) {
        this.predecessorUpdated = predecessorUpdated;
    }

    /**
     * @param successorUpdated the successorUpdated to set
     */
    public void setSuccessorUpdated(boolean successorUpdated) {
        this.successorUpdated = successorUpdated;
    }
}