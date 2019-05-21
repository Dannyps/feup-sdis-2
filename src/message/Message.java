package message;

import message.MessageType;
import java.io.Serializable;
import java.net.InetSocketAddress;

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

    private InetSocketAddress source, destination, realSource;

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

    public static Message handleMessage(Message o) {
        switch (o.getMsgType()) {
        case CHORD_JOIN:
            return (Message<Integer>) o;
        default:
            break;
        }
        return null;
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
}