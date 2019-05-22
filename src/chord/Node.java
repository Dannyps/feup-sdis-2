package chord;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import message.Message;
import message.MessageType;
import utils.PrintMessage;

public class Node {
    public final static int m = 8;
    ChordKey key;
    private InetSocketAddress myAddress = null;
    private InetSocketAddress successor = null;
    private InetSocketAddress predecessor = null;

    private SSLServerSocket socket;
    private AtomicReferenceArray<InetSocketAddress> fingerTable;
    private ConcurrentHashMap<ChordKey, Serializable> data;

    public Node(InetSocketAddress myId, InetSocketAddress peer) {
        this.myAddress = myId;
        this.key = new ChordKey(this.myAddress);
        this.fingerTable = new AtomicReferenceArray<>(m);
        this.data = new ConcurrentHashMap<ChordKey, Serializable>();

        this.socket = createSocket(myId);
        if (peer != null) {
            join(peer);
            // this.write(peer, myId);
        }
        this.read();
    }

    /**
     * Obtains the nth node in the local finger table
     */
    public InetSocketAddress getNthFinger(int n) {
        return fingerTable.get(n);
    }

    /**
     * Sets the nth node in the local finger table
     */
    public void setNthFinger(int n, InetSocketAddress address) {
        PrintMessage.w("Chord", "Setting finger at i=" + n + " -> " + address);
        // if (n == 0) // successor
        // this.notify(address);
        fingerTable.set(n, address);
    }

    /**
     * Notifies the immediate successor, informing that this is its (alive)
     * predecessor
     */
    public boolean notify(InetSocketAddress successor) {
        PrintMessage.w("Chord", "Notifying " + successor + ".");
        if (successor.equals(this.getAddress())) {
            PrintMessage.w("Chord", "Notifying self. Successor not set.");
            return false;
        }

        /*
         * Message<Serializable> notification =
         * Message.makeRequest(Message.Type.AM_YOUR_PREDECESSOR, getAddress(),
         * getAddress()); Message response = dispatcher.sendRequest(successor,
         * notification); return response.getType() == Message.Type.OK;
         */
        return false;
    }

    private void join(InetSocketAddress peer) {
        try {
            Message<Integer> message = new Message<Integer>(MessageType.CHORD_JOIN, myAddress.getPort());
            Message<ChordKey> response = (Message<ChordKey>) write(peer, message, true); // messageJoin
            PrintMessage.w("Response", response.getArg().toString());
            this.successor = response.getSource();
            this.predecessor = response.getSource();
            System.out.println(new ChordKey(successor));
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
        return predecessor;
    }

    /**
     * 
     * @return the successor
     */
    public InetSocketAddress getSuccessor() {
        return successor;
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
                Message o = (Message) ois.readObject();
                o.setRealSource((InetSocketAddress) addr);
                Message res = takeCareOfMessage(o);
                if (o.shouldSendResponse()) {
                    ObjectOutputStream out = new ObjectOutputStream(ssls.getOutputStream());
                    res.doesNotExpectResponse();
                    res.setDestination(res.getSource());
                    res.setSource(this.myAddress);
                    out.writeObject(res);
                }
                ssls.close();
            } catch (IOException | ClassNotFoundException e) {
                PrintMessage.e("Error", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private Message takeCareOfMessage(Message o) {

        try {

            switch (o.getMsgType()) {
            case CHORD_JOIN:
                return handleJoin(o);
            default:
                break;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        PrintMessage.w("Message", "Received message of type " + o.getMsgType() + " from " + o.getSource() + ".");
        return null;

    }

    private Message<ChordKey> handleJoin(Message<?> o) {
        if (this.successor == null && this.predecessor == null) {
            // the joining node is the second one.
            this.successor = o.getSource();
            this.predecessor = o.getSource();
        }
        Message<ChordKey> m = new Message<ChordKey>(MessageType.CHORD_ACK, this.getKey());
        return m;
    }

    /**
     * 
     * @param peer     the destination peer
     * @param o        the object to send (usually a message)
     * @param response wether a response should be caught from the peer
     * @throws Exception
     */
    public Message<?> write(InetSocketAddress peer, Object o, boolean response) throws Exception {
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
            ((Message<?>) o).setSource(this.myAddress);
            ((Message<?>) o).setDestination(peer);
            if (response)
                ((Message<?>) o).expectsResponse();
            output.writeObject(o);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!response) {
            return null;
        } else {
            // we must wait for a response on this socket.
            ObjectInputStream input = new ObjectInputStream(sslSocket.getInputStream());
            Message<?> o1 = (Message<?>) input.readObject();
            sslSocket.close();
            return o1;
        }

    }

    public Object getObj(ChordKey k){
        int kSucc = k.getSucc();
        int mySucc = this.key.getSucc();
        int predSucc = new ChordKey(this.predecessor).getSucc();
        if(kSucc>predSucc && kSucc <=mySucc){
            // I should have this object
            return this.data.get(k);
        }else{
            //TODO someone else has it
            
        }
        
        return new Object();
    }

    public boolean putObj(ChordKey k, Serializable o){
        int kSucc = k.getSucc();
        int mySucc = this.key.getSucc();
        int predSucc = new ChordKey(this.predecessor).getSucc();
        if(kSucc>predSucc && kSucc <=mySucc){
            // I should have this object
            this.data.put(k, o);
            return true;
        }else{
            //TODO someone else has to store it
            return false;   
            
        }
    }
}