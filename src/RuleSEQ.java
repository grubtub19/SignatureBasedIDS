public class RuleSEQ implements RuleOption {

    int seq_num;

    public RuleSEQ(String details) {
        seq_num = Integer.parseUnsignedInt(details);
    }

    @Override
    public boolean matches(Packet packet) {
        TCPProtocol tcp = packet.getProtocol(TCPProtocol.class);

        if (tcp != null) {
            return seq_num == tcp.sequence_number.getAsInt();
        }
        return false;
    }
}
