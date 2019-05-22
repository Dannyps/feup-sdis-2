package chord;

import java.net.InetSocketAddress;

import message.Message;
import message.MessageType;

/**
 * Runnable_Announce_Peer
 */
public class RunnableAnnouncePeer implements Runnable {

    private Node node = null;
    private AnnouncePeer announce = null;
    private InetSocketAddress destination;

    RunnableAnnouncePeer(Node n, AnnouncePeer announce, InetSocketAddress dest) {
        this.node = n;
        this.announce = announce;
        this.destination = dest;
    }

    @Override
    public void run() {
        if (node == null || announce == null) {
            return;
        }
        Message<AnnouncePeer> m = new Message<AnnouncePeer>(MessageType.CHORD_ANNOUNCE_PEER, announce);
        try {
            node.write(destination, m, false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }   
}