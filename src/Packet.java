import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;


public class Packet {
    Protocol first_protocol;
    byte[] body;
    boolean assembled;

    public Packet() {
        assembled = false;
    }

    public Packet(Packet packet) {
        assembled = true;
        // Copy the first_protocol from the original
        first_protocol = copyProtocol(packet.first_protocol);

        // Increment original protocol to be current
        Protocol original_protocol = packet.first_protocol.next_protocol;

        // Save the previous copy protocol for linkage
        Protocol last_protocol = first_protocol;

        // The current protocol that matches up with original_protocol
        Protocol curr_protocol;

        // While the current original protocol is not a dead end
        while(original_protocol != null) {
            // Copy the original
            curr_protocol = copyProtocol(original_protocol);

            // Link old and new copies
            last_protocol.next_protocol = curr_protocol;
            curr_protocol.prev_protocol = last_protocol;

            // Save the current as the previous for next time
            last_protocol = curr_protocol;
            // Get the next original protocol
            original_protocol = original_protocol.next_protocol;
        }
    }

    public static Packet combinePackets(Packet packet, Packet packet2) {
        Packet packet_copy = new Packet(packet);
        Packet packet2_copy = new Packet(packet2);

        packet2_copy.first_protocol.prev_protocol = packet_copy.getLastProtocol();
        packet_copy.getLastProtocol().next_protocol = packet2_copy.first_protocol;
        packet2_copy.first_protocol = packet_copy.first_protocol;

        return packet2_copy;
    }
    private Protocol copyProtocol(Protocol original_protocol) {
        Protocol result;
        if (original_protocol instanceof ARPProtocol) {
            result = new ARPProtocol(this, (ARPProtocol) original_protocol);
        } else if (original_protocol instanceof EthernetProtocol) {
            result = new EthernetProtocol(this, (EthernetProtocol) original_protocol);
        } else if (original_protocol instanceof ICMPProtocol) {
            result = new ICMPProtocol(this, (ICMPProtocol) original_protocol);
        } else if (original_protocol instanceof IPProtocol) {
            result = new IPProtocol(this, (IPProtocol) original_protocol);
        } else if (original_protocol instanceof TCPProtocol) {
            result = new TCPProtocol(this, (TCPProtocol) original_protocol);
        } else if (original_protocol instanceof UDPProtocol) {
            result = new UDPProtocol(this, (UDPProtocol) original_protocol);
        } else {
            result = null;
            System.err.println("Packet.copyProtocol(): original_protocol is null");
        }
        return result;
    }

    public void parse(ByteBuffer bytes) {
        EthernetProtocol ethernet = new EthernetProtocol(this);
        first_protocol = ethernet;

        ParseParams params = new ParseParams(bytes.get(), bytes);
        body = ethernet.parse(params);
    }

    public void parse(Datagram datagram, Protocol protocol) {
        assembled = true;
        first_protocol = protocol;
        ByteBuffer buffer = datagram.getByteBuffer();
        ParseParams params = new ParseParams(buffer.get(), buffer);
        body = protocol.parse(params);
    }

    public boolean moreFragments() {
        IPProtocol ip = getProtocol(IPProtocol.class);
        if (ip != null) {
            return ip.moreFragments();
        }
        return false;
    }

    /**
     * Whether of not the options specify that we should filter out this packet
     * @return
     */
    public boolean isFiltered() {
        Options options = Driver.get().options;

        // If we're filtering by protocol and it's not the specified one
        if (options.protocol != null) {

            // We can only check protocols past IP if its assembled
            if (assembled && options.protocol != ProtocolType.ip && options.protocol != ProtocolType.eth
                    && !hasProtocol(options.protocol)) {
                return true;
            }

            // We can only check ip or eth on fragments because we don't know what the assembled datagram has
            if (!assembled && (options.protocol == ProtocolType.ip || options.protocol == ProtocolType.eth)
                    && !hasProtocol(options.protocol)) {
                return true;
            }
        }

        // If we're filtering by source address and the addresses don't match, filter
        if (options.source_address != null && !options.source_address.equals(getSrcAddress())) {
            return true;
        }

        // If we're filtering by destination address and the addresses don't match, filter
        if (options.destination_address != null && !options.destination_address.equals(getDestAddress())) {
            return true;
        }

        // If we're filtering by either addresses, if neither addresses match, filter
        if (options.or_source_address != null && options.or_destination_address != null &&
                !(options.or_source_address.equals(getSrcAddress())
                        || options.or_destination_address.equals(getDestAddress()))) {
            return true;
        }

        // If we're filtering by both addresses and neither addresses match, filter
        if (options.and_source_address != null && options.and_destination_address != null &&
                !(options.and_source_address.equals(getSrcAddress())
                        && options.and_destination_address.equals(getDestAddress()))) {
            return true;
        }

        // If we're filtering by a range of source ports and the port isn't within the range, filter
        if (options.source_port_start != -1 && options.source_port_end != -1 &&
                !(getSrcPort() >= options.source_port_start && getSrcPort() <= options.source_port_end)) {
            return true;
        }

        // If we're filtering by a range of source ports and the port isn't within the range, filter
        if (options.destination_port_start != -1 && options.destination_port_end != -1 &&
                !(getDestPort() >= options.destination_port_start && getDestPort() <= options.destination_port_end)) {
            return true;
        }
        return false;
    }

    private String getSrcAddress() {
        IPProtocol ip = getProtocol(IPProtocol.class);
        if (ip != null) {
            return ip.source_ip_address.getAsIP();
        }
        ARPProtocol arp = getProtocol(ARPProtocol.class);
        if (arp != null) {
            return arp.sender_protocol_address.getAsIP();
        }
        System.err.println("No Source Address");
        return "";
    }

    public String getDestAddress() {
        IPProtocol ip = getProtocol(IPProtocol.class);
        if (ip != null) {
            return ip.destination_ip_address.getAsIP();
        }
        ARPProtocol arp = getProtocol(ARPProtocol.class);
        if (arp != null) {
            return arp.target_protocol_address.getAsIP();
        }
        System.err.println("No source address in this packet");
        return "";
    }

    public int getSrcPort() {
        TCPProtocol tcp = getProtocol(TCPProtocol.class);
        if (tcp != null) {
            return tcp.source_port.getAsInt();
        }
        UDPProtocol udp = getProtocol(UDPProtocol.class);
        if (udp != null) {
            return udp.source_port.getAsInt();
        }
        //System.err.println("No source port in this packet");
        return -1;
    }

    public int getDestPort() {
        TCPProtocol tcp = getProtocol(TCPProtocol.class);
        if (tcp != null) {
            return tcp.destination_port.getAsInt();
        }
        UDPProtocol udp = getProtocol(UDPProtocol.class);
        if (udp != null) {
            return udp.destination_port.getAsInt();
        }
        //System.err.println("No destination port in this packet");
        return -1;
    }

    public boolean isACK() {
        TCPProtocol tcp = getProtocol(TCPProtocol.class);
        if (tcp != null) {
            return tcp.ack.getBit(0);
        }
        return false;
    }

    public Protocol getProtocol(ProtocolType type) {
        return getProtocol(type.getAsClass());
    }

    public <T extends Protocol> T getProtocol(Class<T> protocol_class) {
        Protocol i_protocol = first_protocol;
        while (i_protocol != null) {

            if (protocol_class.isInstance(i_protocol)) {
                return (T) i_protocol;
            } else {
                i_protocol = i_protocol.next_protocol;
            }
        }
        return null;
    }

    private boolean hasProtocol(ProtocolType type) {
        return getProtocol(type) != null;
    }

    public <T extends Protocol> boolean hasProtocol(Class<T> protocol_class) {
        return getProtocol(protocol_class) != null;
    }

    public Protocol getLastProtocol() {
        Protocol curr_protocol = first_protocol;

        while (true) {
            if(curr_protocol.next_protocol == null) {
                return curr_protocol;
            }
            curr_protocol = curr_protocol.next_protocol;
        }
    }

    /**
     * return either the whole packet or just the protocol from options as a String
     */
    @Override
    public String toString() {
        return toStringAll();
    }

    /**
     * Print the protocols that the options tell us to print and the data
     * @return
     */
    public String toStringAll() {
        StringBuilder result = new StringBuilder();
        result.append(toStringHeaders());
        result.append(toStringData());
        return result.toString();
    }

    /**
     * Print the protocols that the options tell us to print
     * @return String
     */
    public String toStringHeaders() {
        StringBuilder result = new StringBuilder();
        Protocol next = first_protocol;
        while (next != null) {
            if (Driver.get().options.shouldPrint(next)) {
                result.append(next.toString());
            }
            next = next.next_protocol;
        }
        return result.toString();
    }

    private String toStringData() {
        StringBuilder result = new StringBuilder();

        result.append("Data Hex:    <");
        if (body != null) {
            for (byte b : body) {
                result.append(String.format("%02X", b));
            }
        }
        result.append(">\nData String: <");

        if (body != null) {
            // We must remove all \r to avoid strange formatting from occurring
            result.append(new String(body).replaceAll("\\r", ""));
        }
        result.append(">\n");

        return result.toString();
    }
}
