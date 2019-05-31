package utils;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import chord.ChordKey;

public class OurFile implements Serializable {
    String filename;
    int totalReplicationDegree;

    HashMap<Integer, ChordKey> fileDegreeKey;

    int fileReplicationDegree;
    ChordKey key;

    public OurFile(String filename, int totalReplicationDegree) {
        this.filename = filename;
        this.totalReplicationDegree = totalReplicationDegree;
        fileDegreeKey = new HashMap<Integer, ChordKey>();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getTotalReplicationDegree() {
        return totalReplicationDegree;
    }

    public void setTotalReplicationDegree(int totalReplicationDegree) {
        this.totalReplicationDegree = totalReplicationDegree;
    }

    public HashMap<Integer, ChordKey> getFileDegreeKey() {
        return fileDegreeKey;
    }

    public void setFileDegreeKey(HashMap<Integer, ChordKey> fileDegreeKey) {
        this.fileDegreeKey = fileDegreeKey;
    }

    
}
