package chord;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import utils.PrintMessage;

/**
 * ChordKey
 */
public class ChordKey implements Serializable {

    private static final long serialVersionUID = -281700698440823064L;
    private BigInteger key;
    private int succ; // where the object pertaining to this key should be stored

    public ChordKey(InetSocketAddress node) {
        InetAddress ip = node.getAddress();
        int port = node.getPort();
        int intIP = ip.hashCode();
        BigInteger data = BigInteger.valueOf(intIP * port);
        this.key = hashData(data.toByteArray());
        Integer a = (int) Math.pow(2, Node.m);
        this.succ = this.key.abs().mod( BigInteger.valueOf(a.intValue())).intValue();
        this.succ %= Math.pow(2, Node.m);
        PrintMessage.w("KEY", this.key.toString(0));
        PrintMessage.w("KEY", Integer.toString(this.succ));
    }

    private static BigInteger hashData(byte[] data) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        messageDigest.update(data);
        return new BigInteger(messageDigest.digest());
    }

    /**
     * @return the key
     */
    public BigInteger getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "Key: " + this.key.toString() + " Succ: " + Integer.toString(this.succ);
    }

    /**
     * @return the succ
     */
    public int getSucc() {
        return succ;
    }

}