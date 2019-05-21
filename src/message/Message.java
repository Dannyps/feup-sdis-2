package message;

import message.MessageType;
import java.io.Serializable;

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
     * Constructs a new serializable message
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

}