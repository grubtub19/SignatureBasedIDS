public class RuleICode implements RuleOption {

    int icode;

    public RuleICode(String details) {
        icode = Integer.parseUnsignedInt(details);
    }

    @Override
    public boolean matches(Packet packet) {
        ICMPProtocol icmp = packet.getProtocol(ICMPProtocol.class);
        if (icmp != null) {
            return icode == icmp.code.getAsInt();
        }
        return false;
    }
}
