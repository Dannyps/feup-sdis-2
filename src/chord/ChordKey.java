package chord;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ChordKey
 */
public class ChordKey {

    private int key;

    public ChordKey(Node node) {
        Inet4Address ip = (Inet4Address) node.getAddress().getAddress();
        int port = node.getAddress().getPort();
        int intIP = ip.hashCode();
        BigInteger data = BigInteger.valueOf(intIP * port);
        this.key = hashData(data.toByteArray()).hashCode();
    }

    private static String hashData(byte[] data) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        messageDigest.update(data);
        String encryptedString = new String(messageDigest.digest());

        return encryptedString;
    }

    /**
     * @return the key
     */
    public int getKey() {
        return key;
    }

    @Override
    public String toString(){
        return Integer.toString(this.key);
    }

}