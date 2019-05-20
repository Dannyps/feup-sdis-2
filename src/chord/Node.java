package chord;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Node {
    private static final short SOCKET_TIMEOUT = 5000;

    ChordKey key;
    InetSocketAddress myAddress;

    AtomicReferenceArray<InetSocketAddress> fingerTable = new AtomicReferenceArray<InetSocketAddress>(32);

    private SSLServerSocket socket;

    public Node(InetSocketAddress myId, InetSocketAddress peer) {
        this.myAddress = myId;
        this.key = new ChordKey(this);
        this.socket = createSocket(myId);
        this.read();
    }

    public Node(InetSocketAddress myId) {
        this(myId, null);
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

    private static SSLServerSocket createSocket(InetSocketAddress myId) {
        int port = myId.getPort();

        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket;

        try {
            sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());/**/

        } catch (IOException e) {
            throw new RuntimeException("Failed opening port " + port + ".", e);
        }

        sslServerSocket.setNeedClientAuth(true);

        return sslServerSocket;
    }

    public void read() {
        final SSLSocket ssls;

        try { // block waiting for connections
            ssls = (SSLSocket) this.socket.accept();
            ObjectInputStream ois = new ObjectInputStream(ssls.getInputStream());
            Object o = ois.readObject();
            System.out.println(o.getClass());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to read message.", e);
        }

    }

}