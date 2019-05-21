package chord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import message.Message;
import message.MessageType;
import utils.ConsoleColours;
import utils.PrintMessage;

public class Node {
    public final int m = 8;
    ChordKey key;
    InetSocketAddress myAddress;

    AtomicReferenceArray<InetSocketAddress> fingerTable = new AtomicReferenceArray<InetSocketAddress>(32);

    private SSLServerSocket socket;

    public Node(InetSocketAddress myId, InetSocketAddress peer) {
        this.myAddress = myId;
        this.key = new ChordKey(this);

        this.socket = createSocket(myId);
        if (peer != null) {
            join(peer);
            // this.write(peer, myId);
        }
        this.read();
    }

    private void join(InetSocketAddress peer) {
        try {
            write(peer, new Message<>(MessageType.CHORD_JOIN)); // messageJoin
        } catch (Exception e) {
            PrintMessage.e("Error", "The specified peer is not reachable.");
            e.printStackTrace();
            System.exit(5);
        }
        PrintMessage.s("Join", "Join message sent successfully.");
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
            sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());

        } catch (IOException e) {
            throw new RuntimeException("Failed opening port " + port + ".", e);
        }

        sslServerSocket.setNeedClientAuth(true);

        return sslServerSocket;
    }

    @SuppressWarnings("rawtypes")
    public void read() {
        SSLSocket ssls;
        while (true) {
            try { // waiting for connections
                ssls = (SSLSocket) this.socket.accept();
                SocketAddress addr = ssls.getRemoteSocketAddress();
                ObjectInputStream ois = new ObjectInputStream(ssls.getInputStream());
                Object o = ois.readObject();
                takeCareOfMessage((Message) o, addr);
            } catch (IOException | ClassNotFoundException e) {
                PrintMessage.e("Error", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void takeCareOfMessage(Message o, SocketAddress addr) {
        PrintMessage.w("Message", "Received message of type " + o.getMsgType() + " from " + addr.toString() + ".");
    }

    public void write(InetSocketAddress peer, Object o) throws Exception {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = null;

        try {
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(peer.getAddress(), peer.getPort());
        } catch (IOException e) {
            throw new Exception(e);
        }

        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

        try {
            ObjectOutputStream output = new ObjectOutputStream(sslSocket.getOutputStream());
            output.writeObject(o);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}