package chord;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Node{
    ChordKey key;
    InetSocketAddress myAddress;


    AtomicReferenceArray<InetSocketAddress> fingerTable = new AtomicReferenceArray<InetSocketAddress>(32);

    /**
     * Join peer's chord net
     * @param size
     * @param peer
     */
    public Node(int size, InetSocketAddress myId, InetSocketAddress peer){
        this.myAddress = myId;
        this.key = new ChordKey(this);
        System.out.println(this.key);
    }

    /**
     * Create a chord net
     * @param size
     */
    public Node(int size, InetSocketAddress myId){
        this(size, myId, null);
    }

    /**
     * @return the key
     */
    public ChordKey getKey() {
        return key;
    }

    /**
     * @return the myAddress
     */
    public InetSocketAddress getAddress() {
        System.out.println(myAddress);
        return myAddress;
    }

    /**
     * 
     * @return the predecessor
     */
    public InetSocketAddress getPredecessor() {
        // TODO
        return null;
    }

    /**
     * 
     * @return the successor
     */
    public InetSocketAddress getSuccessor() {
        // TODO
        return null;
    }

}