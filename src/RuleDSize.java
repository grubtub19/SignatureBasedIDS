public class RuleDSize implements RuleOption {

    int dsize;

    public RuleDSize(String details) {
        dsize = Integer.parseUnsignedInt(details);
    }

    /**
     * If the dsize is greater than a number
     * @param packet
     * @return
     */
    @Override
    public boolean matches(Packet packet) {
        return packet.body.length >= dsize;
    }
}
