import java.io.Serializable;

/**
 * The Message class represents a message exchanged between peers in the network.
 * It implements the Serializable interface to enable the message object to be serialized
 * and transmitted over a network.
 */
public class Message implements Serializable {
    // A unique identifier for the serialized class (used during deserialization)
    private static final long serialVersionUID = 1L;

    // The actual content of the message (e.g., a word or string)
    private final String content;

    // A timestamp to capture the logical clock value (Lamport Clock)
    private final long timestamp;

    // The port number of the peer that sent this message
    private final int senderPort;

    /**
     * Constructor to initialize a new Message object.
     *
     * @param content    The content of the message.
     * @param timestamp  The logical clock value when the message was created.
     * @param senderPort The port number of the sender.
     */
    public Message(String content, long timestamp, int senderPort) {
        this.content = content;
        this.timestamp = timestamp;
        this.senderPort = senderPort;
    }

    /**
     * Gets the content of the message.
     *
     * @return The content as a String.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the timestamp of the message.
     *
     * @return The timestamp as a long.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the sender's port number.
     *
     * @return The port number as an int.
     */
    public int getSenderPort() {
        return senderPort;
    }

    /**
     * Returns a string representation of the message object.
     * This is useful for debugging and logging purposes.
     *
     * @return A formatted string with message details.
     */
    @Override
    public String toString() {
        return String.format("Message{content='%s', timestamp=%d, senderPort=%d}", content, timestamp, senderPort);
    }
}
