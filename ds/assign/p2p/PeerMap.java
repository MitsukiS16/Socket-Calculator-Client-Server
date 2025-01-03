import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * PeerMap class manages the mapping of peers and their last communication timestamps.
 * 
 * Responsibilities:
 * 1. Store peer addresses and their corresponding timestamps.
 * 2. Merge peer maps received from other peers to synchronize the network state.
 * 3. Remove stale entries based on a specified time threshold.
 * 4. Serialize the peer map to a string for transmission and deserialize it back.
 */
public class PeerMap {

    private Map<Map.Entry<String, Integer>, Instant> peerTimestamps; // Map of peer addresses and their last timestamps
    private String senderAddress; // The address of the peer sending this map

    /**
     * Constructor to initialize a PeerMap instance.
     *
     * @param senderAddress Address of the peer owning this map.
     */
    public PeerMap(String senderAddress) {
        this.peerTimestamps = new HashMap<>();
        this.senderAddress = senderAddress;
    }

    /**
     * Merges another PeerMap into the current map.
     * 
     * Logic:
     * - If a peer already exists in the map, update its timestamp only if the new timestamp is more recent.
     * - If a peer does not exist in the map, add it.
     *
     * @param other The other PeerMap to merge into the current map.
     */
    public void merge(PeerMap other) {
        for (Map.Entry<Map.Entry<String, Integer>, Instant> entry : other.peerTimestamps.entrySet()) {
            Map.Entry<String, Integer> key = entry.getKey(); // Peer address and port
            Instant timestamp = entry.getValue(); // Last update timestamp

            if (peerTimestamps.containsKey(key)) {
                Instant existingTimestamp = peerTimestamps.get(key);
                if (timestamp.isAfter(existingTimestamp)) { // Update only if the new timestamp is later
                    peerTimestamps.put(key, timestamp);
                }
            } else {
                peerTimestamps.put(key, timestamp); // Add new peer
            }
        }
    }

    /**
     * Removes stale entries from the peer map.
     * 
     * Logic:
     * - If a peer's last timestamp is older than the specified threshold, it is removed.
     *
     * @param threshold The duration after which a peer is considered stale.
     */
    public void removeStaleEntries(Duration threshold) {
        Instant now = Instant.now(); // Current time
        peerTimestamps.entrySet().removeIf(entry -> Duration.between(entry.getValue(), now).compareTo(threshold) > 0);
    }

    /**
     * Serializes the PeerMap into a string for transmission.
     * 
     * Format:
     * - First line: Sender's address
     * - Following lines: Each peer's address, port, and timestamp, separated by commas
     * 
     * @return Serialized string representation of the PeerMap.
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(senderAddress).append("\n"); // Include sender address at the top

        for (Map.Entry<Map.Entry<String, Integer>, Instant> entry : peerTimestamps.entrySet()) {
            Map.Entry<String, Integer> key = entry.getKey(); // Peer address and port
            Instant timestamp = entry.getValue(); // Timestamp
            sb.append(key.getKey()).append(",").append(key.getValue()).append(",")
              .append(timestamp.toString()).append("\n"); // Append peer details
        }
        return sb.toString();
    }

    /**
     * Deserializes a string into a PeerMap object.
     * 
     * Logic:
     * - First line is treated as the sender's address.
     * - Remaining lines are parsed to extract peer details (address, port, timestamp).
     *
     * @param data The serialized string representation of a PeerMap.
     * @return The deserialized PeerMap object, or null if the input is invalid.
     */
    public static PeerMap deserialize(String data) {
        String[] lines = data.split("\n"); // Split input into lines
        if (lines.length == 0) {
            return null; // Invalid input
        }

        String senderAddress = lines[0]; // First line is the sender's address
        PeerMap peerMap = new PeerMap(senderAddress);

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue; // Skip empty lines

            String[] parts = line.split(",");
            if (parts.length == 3) { // Each line must have 3 parts: address, port, timestamp
                String peerAddress = parts[0]; // Peer address
                int port = Integer.parseInt(parts[1]); // Peer port
                Instant timestamp = Instant.parse(parts[2]); // Timestamp

                // Add the peer to the map
                peerMap.peerTimestamps.put(new AbstractMap.SimpleEntry<>(peerAddress, port), timestamp);
            }
        }
        return peerMap;
    }

    /**
     * Returns the map of peer addresses and their timestamps.
     *
     * @return The map of peer entries and timestamps.
     */
    public Map<Map.Entry<String, Integer>, Instant> getPeerTimestamps() {
        return peerTimestamps;
    }
}
