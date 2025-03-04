package chord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import message.KeyVal;
import message.Message;
import message.MessageType;
import utils.OurFile;
import utils.AddrPort;
import utils.PrintMessage;
import utils.FileSystemNIO;

public class Node {
    public final static int m = 14;
    private static final String FILE_PREFIX = "file_";
    ChordKey key;
    private InetSocketAddress myAddress = null;
    private InetSocketAddress predecessor = null;

    private SSLServerSocket socket;
    private AtomicReferenceArray<InetSocketAddress> fingerTable;
    private ConcurrentHashMap<ChordKey, Serializable> data;
    private ThreadPoolExecutor executor;

    private File backupFolder;
    private File restoreFolder;
    private ConcurrentHashMap<String, OurFile> fNameKeys;
    private long lastBreathCaught = System.currentTimeMillis();

    public Node(InetSocketAddress myId, InetSocketAddress peer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            leaveChord();
        }));
        this.myAddress = myId;
        this.key = new ChordKey(this.myAddress);
        PrintMessage.i("Key", "My Chord Key is " + this.key.getSucc());
        this.fingerTable = new AtomicReferenceArray<>(m);
        this.data = new ConcurrentHashMap<ChordKey, Serializable>();
        this.fNameKeys = new ConcurrentHashMap<String, OurFile>();
        this.socket = createSocket(myId);
        if (peer != null) {
            PrintMessage.w("Join", "Joining " + peer.toString());
            join(peer);

            // TestClass test = new TestClass(3);
            // putObj(new ChordKey(test), test);
            // this.write(peer, myId);
        }
        this.executor = new ThreadPoolExecutor(5, 10, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        new Thread(new RunnableReader(this)).start(); // leave the reader running.
        new Thread(() -> {
            try {
                printStatus();
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            beginBreathe();
        }).start();

        createFolders();

        readData();
    }

    public void writeData() {
        File file = new File("peer_" + key.getSucc() + File.separator + "data_peer_" + key.getSucc());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(fNameKeys);
            oos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void readData() {
        File file = new File("peer_" + key.getSucc() + File.separator + "data_peer_" + key.getSucc());
        if (file.exists() && file.isFile()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                this.fNameKeys = (ConcurrentHashMap<String, OurFile>) ois.readObject();
                ois.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void leaveChord() {
        PrintMessage.e("Leave", "Shutting down gracefully.");
        try {
            Message s1 = this.write(this.predecessor,
                    new Message<InetSocketAddress>(MessageType.CHORD_LEAVING, this.getSuccessor()), true);
            if (s1 != null && s1.getMsgType() == MessageType.CHORD_ACK) {
                // success
                Thread.sleep(5000);
                PrintMessage.i("Leave", "The chord network has ignored me. Time to launch the last backups.");
                File[] directoryListing = this.backupFolder.listFiles();
                // for each key putObj(key, o)
                for (var f : directoryListing) {
                    ChordKey k = new ChordKey(f.getName().split(FILE_PREFIX)[1]);
                    putObjRemote(k, Files.readAllBytes(f.toPath()));
                    f.delete();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.executor.shutdown();
    }

    private void beginBreathe() {
        do {
            if (System.currentTimeMillis() - this.lastBreathCaught > 10000 && this.getSuccessor() != null) {
                try {
                    breathe();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }

    private void breathe() throws Exception {
        Message<?> m = new Message<>(MessageType.CHORD_BREATHE);
        m.setRealSource(this.myAddress);
        this.write(this.getSuccessor(), m, false);
    }

    private void createFolders() {

        File peerFolder = new File("peer_" + key.getSucc());
        if (!peerFolder.isDirectory() || !peerFolder.exists()) {
            peerFolder.mkdir();
        }

        backupFolder = new File(peerFolder.getAbsolutePath() + "/backup");
        if (!backupFolder.isDirectory() || !backupFolder.exists()) {
            backupFolder.mkdir();
        }

        restoreFolder = new File(peerFolder.getAbsolutePath() + "/restore");
        if (!restoreFolder.isDirectory() || !restoreFolder.exists()) {
            restoreFolder.mkdir();
        }
    }

    private void printStatus() throws InterruptedException {
        while (true) {
            if (this.getSuccessor() == null || this.predecessor == null) {
                Thread.sleep(5000);
                continue;
            }
            PrintMessage.d("Predecessor : ", Integer.toString(new ChordKey(this.predecessor).getSucc()));
            PrintMessage.d("Me          : ", Integer.toString(this.getKey().getSucc()));
            PrintMessage.d("Successor   : ", Integer.toString(new ChordKey(this.getSuccessor()).getSucc()));
            Thread.sleep(5000);
        }
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
        if (address != null)
            PrintMessage.w("Chord",
                    "Setting finger at i=" + n + " -> " + Integer.toString(new ChordKey(address).getSucc()));
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
            Message<?> response = write(peer, message, true); // messageJoin
            setNthFinger(0, response.getSource());
            this.predecessor = response.getSource();
        } catch (Exception e) {
            PrintMessage.e("Error", "The specified peer is not reachable.");
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
        return getNthFinger(0);
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
            SocketAddress addr = null;
            try { // waiting for connections
                ssls = (SSLSocket) this.socket.accept();
                addr = ssls.getRemoteSocketAddress();
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
                PrintMessage.e("Error",
                        e.getMessage() + " received from " + addr.toString() + ". The message has been dropped.");
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Message<?> takeCareOfMessage(Message o) {

        PrintMessage.w("Message", "Received message of type " + o.getMsgType() + " from " + o.getSource() + ".");

        try {
            switch (o.getMsgType()) {
            case CHORD_JOIN:
                return handleJoin((Message<Integer>) o);
            case CHORD_PUT:
                return handlePut(o);
            case CHORD_DEL:
                return handleDel(o);
            case CHORD_ANNOUNCE_PEER:
                handleAnnouncePeer(o);
                break;
            case CHORD_PREDECESSOR_HERE:
                return handlePredecessorHere(o);
            case CHORD_LOOKUP:
                return handleGet(o);
            case CHORD_BREATHE:
                handleBreathe(o);
                break;
            case CHORD_LEAVING:
                return handleLeaving(o);
            default:
                break;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
    private Message handleLeaving(Message<InetSocketAddress> o) {
        if (AddrPort.compareHosts(o.getArg(), this.myAddress)) {
            // would set self as successor. It shall be set as null,
            // because the only other peer in the cord network is going away.
            setNthFinger(0, null);
            this.predecessor = null;
        } else {
            this.setNthFinger(0, o.getArg());
        }
        try {
            breathe();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Message<>(MessageType.CHORD_ACK);

    }

    @SuppressWarnings("rawtypes")
    private void handleBreathe(Message o) {
        PrintMessage.i("Breathe", "It breathes. It is alive.");
        this.lastBreathCaught = System.currentTimeMillis();
        if (o.inHopList(this.myAddress)) {
            PrintMessage.e("learn found me!", "");
            learnNodes(o.getHopList());
        }
        if (o.inHopListTwice(this.getSuccessor())) {
            // I'm breathing old air. I won't allow this!
            PrintMessage.e("Breathe", "I'm breathing old air. I won't allow this!");
            return;
        }
        try {
            this.write(this.getSuccessor(), o, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void learnNodes(LinkedList<InetSocketAddress> hopList) {
        // this.myaddress should occour twice in the hopList.
        // All peers between these two occurences are in the chord ring.

        LinkedList<InetSocketAddress> nList = new LinkedList<InetSocketAddress>();

        boolean foundFirst = false;
        for (var h : hopList) {
            if (foundFirst && !nList.isEmpty() && AddrPort.compareHosts(h, this.myAddress)) {
                break; // all nodes are added
            }
            if (foundFirst) {
                nList.add(h);
            }
            if (AddrPort.compareHosts(h, this.myAddress)) {
                foundFirst = true;
            }
        }
        for (int i = 0; i < Node.m; i++) {
            int res = (int) (Math.pow(2, i) % Math.pow(2, Node.m));
            if (res >= nList.size())
                break;
            setNthFinger(i, nList.get(res - 1));
        }

        // update prededessor
        this.predecessor = nList.getLast();

    }

    private Message<Boolean> handlePredecessorHere(Message<InetSocketAddress> o) {
        this.predecessor = o.getSource();
        this.setNthFinger(0, o.getArg());
        PrintMessage.e("Predecessor", "updated: " + this.getSuccessor().toString());
        return new Message<Boolean>(MessageType.CHORD_ACK, true);
    }

    private void handleAnnouncePeer(Message<AnnouncePeer> o) {
        PrintMessage.w("Announce", "handling Announce");
        AnnouncePeer a = o.getArg();
        a.increaseCount();
        PrintMessage.i("Announce", Integer.toString(a.getHopCount()));
        ChordKey npk = a.getNewPeerKey();
        ChordKey pk = new ChordKey(this.predecessor);
        ChordKey sk = new ChordKey(this.getSuccessor());
        PrintMessage.i("Announce", "New predecessor. "
                + String.format("np: %s, pre: %d, me: %d", npk.getSucc(), pk.getSucc(), key.getSucc()));
        PrintMessage.i("Announce", "New successor. "
                + String.format("np: %s, me: %d, suc: %d", npk.getSucc(), key.getSucc(), sk.getSucc()));
        if (npk.getSucc() != this.key.getSucc() && keyInBetween(npk, pk, this.key)) {
            // the new node is my predecessor
            this.predecessor = a.getNewPeerAddress();
            new Thread(() -> {
                relocateData(pk, new ChordKey(this.predecessor));
            }).start();
            a.setPredecessorUpdated();
            PrintMessage.d("d", "predecessor updated here.");
        } else if (keyInBetween(npk, this.key, sk)) {
            // the new node is my successor
            InetSocketAddress oldSuccessor = this.getSuccessor();
            this.setNthFinger(0, a.getNewPeerAddress());
            pokeSuccessor(oldSuccessor); // informing it it's my successor
            a.setSuccessorUpdated();
            PrintMessage.d("d", "successor updated here.");
        } else {
            // predecessor and successor not changed for me because of this new peer, carry
            // on...
            PrintMessage.i("Announce", "I'm unnaffected");
        }

        if (a.mustBeForwarded()) {
            PrintMessage.w("Announce", "Forwarding to " + sk.getSucc());
            executor.execute(new RunnableAnnouncePeer(this, a, this.getSuccessor()));
        } else {
            PrintMessage.w("Announce", "Finished at " + a.getHopCount() + " hops.");
        }
    }

    private void pokeSuccessor(InetSocketAddress oldSuccessor) {
        executor.execute(new RunnablePokeSuccessor(this, oldSuccessor));
    }

    public static boolean keyInBetween(ChordKey k, ChordKey a, ChordKey b) {
        return keyInBetween(k.getSucc(), a.getSucc(), b.getSucc());
    }

    private static boolean keyInBetween(int k, int a, int b) {
        return (a > b && (k > a || k < b)) || (a < k && k <= b);
    }

    private Message<?> handleDel(Message<KeyVal> o) {
        PrintMessage.w("Received DEL", "with arg: " + o.getArg().getVal());
        PrintMessage.w("Data", "Internal data contains " + data.size() + " entries.");
        boolean success = delObj(o.getArg().getKey());
        PrintMessage.w("Data", "Internal data contains " + data.size() + " entries.");
        return new Message<Boolean>(MessageType.CHORD_ACK, success);
    }

    private Message<?> handlePut(Message<KeyVal> o) {
        PrintMessage.w("Received PUT", "with arg: " + o.getArg().getVal());
        PrintMessage.w("Data", "Internal data contains " + data.size() + " entries.");
        boolean success = putObj(o.getArg().getKey(), (Serializable) o.getArg().getVal());
        PrintMessage.w("Data", "Internal data contains " + data.size() + " entries.");
        return new Message<Boolean>(MessageType.CHORD_ACK, success);
    }

    private Message<?> handleGet(Message<ChordKey> o) {
        PrintMessage.w("Received GET", "with arg: " + o.getArg().toString());
        PrintMessage.w("Data", "Internal data contains " + data.size() + " entries.");
        Object ret;
        try {
            ret = getObj(o.getArg());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ret = null;
        }
        PrintMessage.w("Data", "Internal data contains " + data.size() + " entries.");
        return new Message<KeyVal>(MessageType.CHORD_ACK, new KeyVal(o.getArg(), ret));
    }

    /**
     * Someone has connect to this peer to enter the Chord ring. We will inform the
     * rest of the ring of the news.
     * 
     * @param o the Message received
     * @return a response
     */
    private Message<Boolean> handleJoin(Message<Integer> o)

    {
        if (this.getSuccessor() == null && this.predecessor == null) {
            // the joining node is the second one.
            PrintMessage.w("Join", "Handling second join.");
            this.setNthFinger(0, o.getSource());
            this.predecessor = o.getSource();
            return new Message<Boolean>(MessageType.CHORD_ACK, true);
        } else {
            // this is a complex network
            // we must notify "all" nodes of the incoming peer
            // the message bellow must be propagated through the ring.
            AnnouncePeer announce = new AnnouncePeer(new ChordKey(o.getSource()), o.getSource());
            executor.execute(new RunnableAnnouncePeer(this, announce, this.getSuccessor()));
            return new Message<Boolean>(MessageType.CHORD_ACK, false);
        }
    }

    /**
     * 
     * @param ppk the old predecessor's (now a pre-predecessor) key
     * @param pk  the predecessor's key.
     * 
     *            documents whose key are in between these two should be relocated
     *            to the predecessor.
     */
    private void relocateData(ChordKey ppk, ChordKey pk) {
        File[] directoryListing = this.backupFolder.listFiles();
        PrintMessage.d("Relocation", "entered. " + Integer.toString(directoryListing.length) + " files found.");
        for (var f : directoryListing) {
            ChordKey k = new ChordKey(f.getName().split(FILE_PREFIX)[1]);
            if (keyInBetween(k, ppk, pk)) {
                try {
                    boolean success = putObj(k, Files.readAllBytes(f.toPath()));
                    if (success) {
                        PrintMessage.d("Relocation", "deleting " + f.getName());
                        f.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
            PrintMessage.e("FATAL", "could not write message to peer " + peer.toString());
            e.printStackTrace();
        } catch (NullPointerException e) {
            PrintMessage.e("FATAL", "Could not write to null peer!");
            return null;
        }

        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

        try {
            ObjectOutputStream output = new ObjectOutputStream(sslSocket.getOutputStream());
            ((Message<?>) o).setSource(this.myAddress);
            ((Message<?>) o).setDestination(peer);
            ((Message<?>) o).addToHopList(this.myAddress);
            if (response)
                ((Message<?>) o).expectsResponse();
            output.writeObject(o);
        } catch (IOException e) {
            PrintMessage.e("Protocol Mismatch", "Could not interpret the received message!");
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

    @SuppressWarnings("unchecked")
    public Object getObj(ChordKey k) throws Exception {
        int kSucc = k.getSucc();
        int mySucc = this.key.getSucc();
        int predSucc = new ChordKey(this.predecessor).getSucc();

        if (keyInBetween(kSucc, predSucc, mySucc)) {
            // I should have this object

            String filename = backupFolder.getAbsolutePath() + File.separator + FILE_PREFIX + k.getKey().toString();
            PrintMessage.i("lookup", filename);

            Object o = FileSystemNIO.loadLocalFile(filename);
            if (o == null) {
                throw new Exception("value not found");
            }
            PrintMessage.e("GET", "Lookup k-" + k + " v-" + o.getClass());
            return o;
        } else {
            PrintMessage.w("GET", "Forwarding request for " + k.toString());
            Message<ChordKey> m = new Message<ChordKey>(MessageType.CHORD_LOOKUP, k);
            try {
                Message<KeyVal> response = (Message<KeyVal>) this.write(this.getSuccessor(), m, true);
                return response.getArg().getVal();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean putObj(ChordKey key, Serializable o) {
        int k = key.getSucc();
        int m = this.key.getSucc();

        int a = new ChordKey(this.predecessor).getSucc();
        boolean storeLocally = false;

        if (this.getSuccessor() == null) {
            // this node does not have a successor... There is no network yet.
            storeLocally = true;
        } else {
            PrintMessage.d("PutObj", String.format("kSucc: %d mySucc: %d preSucc: %d", k, m, a));
            a = new ChordKey(this.predecessor).getSucc();
        }
        if (storeLocally || keyInBetween(k, a, m)) {
            // I should store this object
            PrintMessage.i("Put", "storing locally: k-" + key + " v-" + "");

            // this.data.put(key, o);
            return FileSystemNIO.storeLocalFile(backupFolder.getAbsolutePath() + File.separator + FILE_PREFIX + key.getKey().toString(), (byte[]) o);
        } else {
            return putObjRemote(key, o);

        }
    }

    private boolean putObjRemote(ChordKey key, Serializable o) {
        PrintMessage.i("Put", "storing remotly");
        // TODO someone else has to store it
        Message<KeyVal> message = new Message<KeyVal>(MessageType.CHORD_PUT, new KeyVal(key, o));
        try {
            Message<Boolean> response = (Message<Boolean>) write(this.getSuccessor(), message, true);
            PrintMessage.w("PUT", "Received " + response.getArg() + "after storing remotly.");
            return response.getArg();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean delObj(ChordKey key) {
        int k = key.getSucc();
        int m = this.key.getSucc();

        int a = new ChordKey(this.predecessor).getSucc();
        boolean deleteLocally = false;

        if (this.getSuccessor() == null) {
            // this node does not have a successor... There is no network yet.
            deleteLocally = true;
        } else {
            PrintMessage.d("PutObj", String.format("kSucc: %d mySucc: %d preSucc: %d", k, m, a));
            a = new ChordKey(this.predecessor).getSucc();
        }
        if (deleteLocally || keyInBetween(k, a, m)) {
            // I should store this object
            PrintMessage.i("Del", "deleting locally: k-" + key + " v-" + "");

            File file = new File(
                    backupFolder.getAbsolutePath() + File.separator + FILE_PREFIX + key.getKey().toString());
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            // this.data.put(key, o);
            return true;
        } else {
            PrintMessage.i("Del", "deleting remotly");
            // TODO someone else has to store it
            Message<KeyVal> message = new Message<KeyVal>(MessageType.CHORD_DEL, new KeyVal(key, key.getSucc()));
            try {
                Message<Boolean> response = (Message<Boolean>) write(this.getSuccessor(), message, true);
                PrintMessage.w("DEL", "Received " + response.getArg() + "after deleting remotly.");
                return response.getArg();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return false;
        }
    }

    public void addFileNameKeyPair(String filename, OurFile ourFile) {
        this.fNameKeys.put(filename, ourFile);
    }

    public void delFileNameKeyPair(String filename) {
        this.fNameKeys.remove(filename);
    }

    /**
     * @return the fNameKeys
     */
    public ConcurrentHashMap<String, OurFile> getfNameKeys() {
        return fNameKeys;
    }

    public File getRestoreFolder() {
        return restoreFolder;
    }
}