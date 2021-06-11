import java.nio.ByteBuffer;
import java.util.ArrayList;


public class Datagram {
    Packet assembled_packet;
    ArrayList<Byte> data;
    ArrayList<Packet> fragments;
    int sid;
    boolean last_received;
    long last_time_updated;

    public Datagram() {
        data = new ArrayList<>();
        fragments = new ArrayList<>();
        sid = 1;
        last_received = false;
        last_time_updated = 0;
    }

    /**
     * Add a packet that has only parsed until the end of the IPProtocol header.
     * @param packet The partially parsed packet
     */
    public void addFragment(Packet packet) {

        IPProtocol ip = packet.getProtocol(IPProtocol.class);
        if (ip != null) {
            // Check if IP datagram is overloaded (Ping-of-Death)
            if (ip.fragment_offset.getAsInt() * 8 + ip.total_length.getAsInt() > 65535) {
                changeSID(3);
            }

            // Check if someone's trying to break up the TCP header (TCP min is 20)
            if (packet.hasProtocol(TCPProtocol.class) && ip.fragment_offset.getAsInt() == 1 ) {
                System.err.println("Attempt to break TCP header");
                // no sid for this
            }

            int offset_bytes = ip.fragment_offset.getAsInt() * 8;
            resize(offset_bytes + packet.body.length);
            addData(packet.body, offset_bytes);

            // Add the packet to the list of fragments
            fragments.add(packet);
            last_time_updated = System.currentTimeMillis();

        }
    }

    public void assemblePacket() {
        // If its an individual packet, don't reassemble
        if (fragments.size() == 1) {
            assembled_packet = fragments.get(0);
            changeSID(0);
        // Else reassemble if its finished
        } else if (finished()) {
            // Get the ip protocol from one of the fragments
            IPProtocol ip = fragments.get(0).getProtocol(IPProtocol.class);
            // Create a Packet for the Datagram's data
            assembled_packet = new Packet();
            // Create the next Protocol
            Protocol next_protocol = ip.getNext(data.size());
            // Parse the Datagram's data using the next Protocol
            assembled_packet.parse(this, next_protocol);
        }
    }

    public Signature getSignature() {
        return new Signature(assembled_packet, fragments, sid);
    }

    //TODO differences between OS
    public void addWindows(byte b, int offset) {
        if (data.get(offset) == null) {
            data.set(offset, b);
        }
    }

    public void addLinux(byte b, int offset) {
        data.set(offset, b);
    }

    /**
     * Datagram is finished if we received no more bytes and there's no gaps in our data.
     * @return boolean
     */
    public boolean finished() {
        if (last_received) {
            for (Byte b : data) {
                if (b == null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void addData(byte[] add_data, int offset) {
        int byte_offset = offset;
        for (byte b : add_data) {
            if (data.get(byte_offset) != null) {
                changeSID(2);
            }
            if (Driver.get().options.os.equals("linux")) {
                addLinux(b, byte_offset);
            } else {
                addWindows(b, byte_offset);
            }
            byte_offset++;
        }
    }

    public void resize(int max_size) {
        int size = data.size();
        for (int i = size; i < max_size; i++) {
            data.add(null);
        }
    }

    public ByteBuffer getByteBuffer() {
        // Setup new ParseParams to parse the Datagram's data
        Byte[] obj_bytes = data.toArray(new Byte[data.size()]);
        byte[] bs = new byte[obj_bytes.length];
        for (int i = 0; i < obj_bytes.length; i++) {
            bs[i] = obj_bytes[i];
        }
        return ByteBuffer.wrap(bs);
    }

    /**
     * The datagram times out if it has been over 30 seconds since the last packet
     * @return boolean
     */
    public boolean timeout() {
        long curr_time = System.currentTimeMillis();

        // Timeout time is 30 seconds or 30000 milliseconds
        return curr_time - last_time_updated > 30000;
    }

    public void changeSID(int new_sid) {
        sid = new_sid;
        //TODO add priority to certain more harmful sids, maybe change sid to a list of booleans
    }

    public String toString() {
        // Use the last fragment's header for printing
        Packet last_fragment = fragments.get(fragments.size() - 1);
        StringBuilder result = new StringBuilder();
        result.append("\nPrinting an Assembled IP Packet: \n");

        // Print the last fragment's headers if the options allow it (header is empty if not)
        String header = last_fragment.toStringHeaders();
        if (!header.isEmpty()) {
            result.append("Last Received Fragment Header:\n" + header);
        }

        // Print the assembled datagram (at the very least the data is printed)
        result.append("Assembled Datagram:\n" + assembled_packet.toString());

        // Print the fragments if enabled by the options
        if (!Driver.get().options.dont_print_fragments) {
            // Print the fragments
            int i = 0;
            for (Packet p : fragments) {
                result.append("\nFragment " + i + ": \n" + p.toString());
                i++;
            }
        }
        return result.toString();
    }
}
