import java.util.ArrayList;

public class Rule {

    String action;
    ProtocolType protocol;
    IPAddress ip1;
    PortRange port_range1;
    boolean bi_dir;
    IPAddress ip2;
    PortRange port_range2;
    ArrayList<RuleOption> options;

    String log_filename;
    String msg;
    int sid = -1;

    public Rule() {
        ip1 = new IPAddress();
        port_range1 = new PortRange();
        ip2 = new IPAddress();
        port_range2 = new PortRange();
        options = new ArrayList<>();
    }

    public boolean matches(Packet packet) {
        // Check if the packet has the specified protocol
        if (!packet.hasProtocol(protocol.getAsClass())) {
            if (packet.hasProtocol(IPProtocol.class)) {
                if(packet.getProtocol(IPProtocol.class).nextProtocol() != protocol) {
                    return false;
                }
            } else {
                return false;
            }
        }
        IPProtocol ip = packet.getProtocol(IPProtocol.class);

        boolean ip1_source = ip1.matches(ip.source_ip_address.getAsInt());
        boolean ip2_source = ip2.matches(ip.source_ip_address.getAsInt());
        boolean ip1_dest = ip1.matches(ip.destination_ip_address.getAsInt());
        boolean ip2_dest = ip2.matches(ip.destination_ip_address.getAsInt());

        boolean port1_source = port_range1.matches(packet.getSrcPort());
        boolean port2_source = port_range2.matches(packet.getSrcPort());
        boolean port1_dest = port_range1.matches(packet.getDestPort());
        boolean port2_dest = port_range2.matches(packet.getDestPort());

        if (packet.getSrcPort() == -1) {
            port1_source = false;
            port2_source = false;
        }
        if (packet.getDestPort() == -1) {
            port1_dest = false;
            port2_dest = false;
        }

        boolean address_match = false;
        // If bi-directional, check both ways
        if (bi_dir) {
            // forward check
            if (ip1_source && ip2_dest && port1_source && port2_dest) {
                address_match = true;
            }
            // reverse check
            if (ip2_source && ip1_dest && port2_source && port1_dest) {
                address_match = true;
            }
        // If single-directional, only check forward
        } else {
            // forward check
            if (ip1_source && ip2_dest && port1_source && port2_dest) {
                address_match = true;
            }
        }

        if (!address_match) {
            return false;
        }

        for (RuleOption option: options) {
            if (!option.matches(packet)) {
                return false;
            }
        }

        // Rule matches, do the stuff
        if (action.equals("alert")) {
            Driver.get().writer.writeRule(packet, log_filename, msg, sid);
        }
        return true;
    }

    public void logFile(Signature signature) {

    }
}
