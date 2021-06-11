public class RuleID implements RuleOption {
    int id;

    public RuleID(String details) {
        id = Integer.parseUnsignedInt(details);
    }

    @Override
    public boolean matches(Packet packet) {
        IPProtocol ip = packet.getProtocol(IPProtocol.class);

        if (ip != null) {
            return id == ip.id.getAsInt();
        }
        return false;
    }
}
