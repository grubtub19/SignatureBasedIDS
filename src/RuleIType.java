public class RuleIType implements RuleOption {

    int itype;

    public RuleIType(String details) {
        itype = Integer.parseUnsignedInt(details);
    }
    @Override
    public boolean matches(Packet packet) {
        ICMPProtocol icmp = packet.getProtocol(ICMPProtocol.class);

        if (icmp != null) {
            return itype == icmp.type.getAsInt();
        }
        return false;
    }
}
