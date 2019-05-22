package chord;

import java.net.InetSocketAddress;

import message.Message;
import message.MessageType;
import utils.PrintMessage;

/**
 * RunnablePokeSuccessor
 */
public class RunnablePokeSuccessor implements Runnable {

    private Node node;
    private InetSocketAddress oldSuccessor;

    RunnablePokeSuccessor(Node node, InetSocketAddress oldSuccessor) {
        this.node = node;
        this.oldSuccessor = oldSuccessor;
    }

    @Override
    public void run() {

        Message<InetSocketAddress> m = new Message<InetSocketAddress>(MessageType.CHORD_PREDECESSOR_HERE, oldSuccessor);
        try {
            Message response = this.node.write(this.node.getSuccessor(), m, true);
            PrintMessage.i("Poke", "Poked successor ("+this.node.getSuccessor().toString()+") successfully?" + response.getMsgType());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}