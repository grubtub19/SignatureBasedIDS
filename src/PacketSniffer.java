import java.nio.ByteBuffer;

/**
 * Sniffs, Parses and Saves packets in both raw byte[] and Packet formats
 */
public class PacketSniffer {
    byte[] bytes;
    Packet packet;
    int packets_sniffed;

    public PacketSniffer() {
        packets_sniffed = 0;
    }

    public void sniff() {
        // Read the packet as byte[]
        bytes = Driver.get().driver.readPacket();

        // Parse a new packet object
        packet = new Packet();
        packet.parse(ByteBuffer.wrap(bytes));
        packets_sniffed++;
    }

    /**
     * If we haven't reached the limit to the number of packets we're sniffing
     * @return
     */
    public boolean stillSniffing() {
        int max_packets = Driver.get().options.count;
        // If there's no limit or we haven't reached the limit yet
        return (max_packets == -1 || packets_sniffed < max_packets);
    }
}
