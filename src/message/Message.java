package message;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import utils.AddrPort;
import utils.PrintMessage;

public class Message<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = -8006817488241702192L;

    /**
     * The message type
     */
    private MessageType msgType;

    /**
     * The data to be sent on the message
     */
    private T arg = null;

    /**
     * filled by the FIRST sender.
     */
    private InetSocketAddress firstSource = null;

    /**
     * filled by the sender
     * 
     * contains the port to where responses should be sent.
     */
    private InetSocketAddress source = null;

    /**
     * filled by the sender
     */
    private InetSocketAddress destination = null;

    /**
     * filled by the receiver
     * 
     * contains the InetSocketAddress as seen from the receiver.
     */
    private InetSocketAddress realSource = null;

    /**
     * wether the sender expects a response
     */
    private boolean response = false;

    private LinkedList<InetSocketAddress> hopList = new LinkedList<InetSocketAddress>();

    /**
     * Constructs a new serializable message
     * 
     * @param msgType
     * @param arg
     */
    public Message(MessageType msgType, T content) {
        this.msgType = msgType;
        this.arg = content;
    }

    public Message(MessageType msgType) {
        this(msgType, null);
    }

    /**
     * @return the msgType
     */
    public MessageType getMsgType() {
        return msgType;
    }

    /**
     * @return the arg
     */
    public T getArg() {
        return arg;
    }

    /**
     * @return the destination
     */
    public InetSocketAddress getDestination() {
        return destination;
    }

    /**
     * @return the source
     */
    public InetSocketAddress getSource() {
        return source;
    }

    /**
     * @return the realSource
     */
    public InetSocketAddress getRealSource() {
        return realSource;
    }

    /**
     * @param firstSource the firstSource to set
     */
    public void setFirstSource(InetSocketAddress firstSource) {
        this.firstSource = firstSource;
    }

    /**
     * @return the firstSource
     */
    public InetSocketAddress getFirstSource() {
        return firstSource;
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * @param realSource the realSource to set
     */
    public void setRealSource(InetSocketAddress realSource) {
        this.realSource = realSource;
    }

    /**
     * @param source the source to set
     */
    public void setSource(InetSocketAddress source) {
        this.source = source;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(InetSocketAddress destination) {
        this.destination = destination;
    }

    public InetSocketAddress getReplyTo() {
        if (realSource != null && source != null) {
            if (this.shouldSendResponse()) { // a socket on the other end is waiting for us.
                return realSource;
            } else { // a new connection must be opened to send this response (should not happen)
                return new InetSocketAddress(realSource.getAddress(), source.getPort());
            }
        } else
            return null;
    }

    /**
     * @return whether this message should return a response (on the same socket)
     */
    public boolean shouldSendResponse() {
        return this.response;
    }

    /**
     * sets this message as expecting a response
     */
    public void expectsResponse() {
        this.response = true;
    }

    /**
     * sets this message as not expecting a response
     */
    public void doesNotExpectResponse() {
        this.response = false;
    }

    /**
     * @return the hopList
     */
    public LinkedList<InetSocketAddress> getHopList() {
        return hopList;
    }

    /**
     * @param hopList the hopList to set
     */
    public boolean addToHopList(InetSocketAddress host) {
        return this.hopList.add(host);
    }

    public boolean inHopListTwice(InetSocketAddress host) {
        int count = 0;
        for (InetSocketAddress h : this.hopList) {
            if (AddrPort.compareHosts(h, host))
                count++;
        }
        return count >= 2;
    }

    public boolean inHopList(InetSocketAddress host) {
        return this.hopList.contains(host);
    }

}