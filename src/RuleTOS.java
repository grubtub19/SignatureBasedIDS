public class RuleTOS implements RuleOption {
    int tos;

    public RuleTOS(String details) {
        tos = Integer.parseUnsignedInt(details);
    }

    @Override
    public boolean matches(Packet packet) {
        IPProtocol ip = packet.getProtocol(IPProtocol.class);

        if (ip != null) {
            // TOS is the old definition of dscp
            return tos == ip.dscp.getAsInt();
        }
        return false;
    }
}
