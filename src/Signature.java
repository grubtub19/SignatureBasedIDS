import java.util.ArrayList;

public class Signature {
    public Packet packet;
    public ArrayList<Packet> fragments;
    public int sid;

    // Individual Packet
    public Signature(Packet individual) {
        packet = individual;
        fragments = new ArrayList<Packet>();
        fragments.add(packet);
        sid = 0;
    }

    // An assembled IP packet with an sid provided by the datagram
    public Signature(Packet assembled, ArrayList<Packet> fragments, int sid) {
        packet = assembled;
        this.fragments = fragments;
        this.sid = sid;
    }

    // Timed-out Datagram (4)
    public Signature(Datagram datagram) {
        packet = datagram.fragments.get(0);
        fragments = datagram.fragments;
        sid = 4;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        // If its an individual packet
        if (sid == 0) {
            result.append("\nPrinting an Individual Packet (SID=" + sid +"): \n");
            result.append(packet.toString());
        } else if (sid == 4) {
            result.append("\nPrinting Timed-Out IP Fragments (SID=" + sid +"): \n");
            result.append(toStringFragments());
        } else {
            // Use the last fragment's header for printing
            Packet last_fragment = fragments.get(fragments.size() - 1);

            result.append("\nPrinting an Assembled IP Packet (SID=" + sid +"): \n");

            // Print the last fragment's headers if the options allow it (header is empty if not)
            String header = last_fragment.toStringHeaders();
            if (!header.isEmpty()) {
                result.append("Last Received Fragment Header:\n" + header);
            }

            // Print the assembled datagram (at the very least the data is printed)
            result.append("Assembled Datagram:\n" + packet.toString());

            result.append(toStringFragments());
        }

        return result.toString();
    }

    private String toStringFragments() {
        StringBuilder result = new StringBuilder();
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
