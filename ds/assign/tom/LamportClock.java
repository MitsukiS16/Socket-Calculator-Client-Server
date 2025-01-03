import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The LamportClock class represents a logical clock based on Lamport's logical clock algorithm.
 * It is used to order events in a distributed system.
 */
public class LamportClock {
    // The current value of the logical clock
    private long time;

    // The port number of the peer using this clock
    private int port;

    /**
     * Constructor to initialize a new LamportClock with the given port number.
     *
     * @param port The port number associated with this clock.
     */
    public LamportClock(int port) {
        this.time = 0; // Initialize the clock to 0
        this.port = port;
    }

    /**
     * Sets the clock's time to a new value.
     * This is used to synchronize the clock with other peers.
     *
     * @param newTime The new time value to set.
     */
    public void setTime(long newTime) {
        this.time = newTime;
    }

    /**
     * Retrieves the current time of the clock.
     *
     * @return The current logical clock value.
     */
    public long getTime() {
        return time;
    }

    /**
     * Retrieves the port number associated with this clock.
     *
     * @return The port number as an int.
     */
    public int getPort() {
        return port;
    }

    /**
     * Increments the logical clock by 1.
     * This is done when an event occurs locally.
     */
    public void increment() {
        this.time++;
    }

    /**
     * Displays the current state of the clock.
     * Useful for debugging and logging purposes.
     *
     * @return A string representation of the clock's state.
     */
    public String showTime() {
        return String.format("LamportClock: port=%d, time=%d", port, time);
    }

    /**
     * Serializes the LamportClock object into a byte array.
     * This is useful for sending the clock's state over a network.
     *
     * @return A byte array representing the serialized clock.
     * @throws IOException If an I/O error occurs during serialization.
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(this); // Serialize the current object
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Deserializes a byte array into a LamportClock object.
     * This is used to reconstruct the clock's state received over a network.
     *
     * @param data The byte array containing the serialized clock data.
     * @return A LamportClock object reconstructed from the data.
     * @throws IOException If an I/O error occurs during deserialization.
     * @throws ClassNotFoundException If the class definition cannot be found.
     */
    public static LamportClock deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

            return (LamportClock) objectInputStream.readObject(); // Deserialize the object
        }
    }
}
