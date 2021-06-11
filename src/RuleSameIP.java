public class RuleSameIP implements RuleOption {


    @Override
    public boolean matches(Packet packet) {
        IPProtocol ip = packet.getProtocol(IPProtocol.class);
        if (ip != null) {
            return ip.source_ip_address.getAsInt() == ip.destination_ip_address.getAsInt();
        }
        return false;
    }
}
