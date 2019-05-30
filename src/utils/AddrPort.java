package utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class AddrPort {
    String address = "";
    int port = 0;

    public AddrPort(String a, int p) {
        address = a;
        port = p;
    }

    public AddrPort(String pair) throws Exception {
        int i = pair.lastIndexOf(":");
        String[] n = {pair.substring(0, i), pair.substring(i+1)};

        if (n.length == 2) {
            address = n[0];
            port = Integer.parseInt(n[1]);
        } else
            throw new Exception("String passed was not of the form addr:port");
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByName(address);
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return address.toString() + ":" + Integer.toString(port);
    }

    public InetSocketAddress getInetSocketAddress(){
        return new InetSocketAddress(address, port);
    }

    public static boolean compareHosts(InetSocketAddress h1, InetSocketAddress h2){
        return h1.getPort() == h2.getPort() && h1.getAddress().getHostAddress().compareTo(h2.getAddress().getHostAddress()) == 0 ;
    }
}