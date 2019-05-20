package message;

import java.awt.TrayIcon.MessageType;
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
    private T content = null;

    /**
     * Constructs a new serializable message
     * @param msgType
     * @param content
     */
    Message(MessageType msgType, T content) {
        this.msgType = msgType;
        this.content = content;
    }

}