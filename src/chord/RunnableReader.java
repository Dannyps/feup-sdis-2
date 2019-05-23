package chord;

/**
 * RunnableReader
 */
public class RunnableReader implements Runnable {


    private Node node;

    public RunnableReader(Node n) {
        this.node = n;
    }

    @Override
    public void run() {
        node.read();
    }

    
}