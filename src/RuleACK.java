public class RuleACK implements RuleOption {

    int ack;

    public RuleACK(String details) {
        ack = Integer.parseUnsignedInt(details);
    }

    @Override
    public boolean matches(Packet packet) {
        return packet.isACK();
    }
}
