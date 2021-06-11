public class RuleFragOffset implements RuleOption {
    public enum Operator {
        equal, notEqual, lessThan, greaterThan
    }
    Operator op;
    int offset;

    public RuleFragOffset(String details) {
        if(Character.isDigit(details.charAt(0))) {
            op = Operator.equal;
            offset = Integer.parseUnsignedInt(details);
        } else {
            switch(details.charAt(0)) {
                case '!':
                    op = Operator.notEqual;
                    break;
                case '<':
                    op = Operator.lessThan;
                    break;
                case '>':
                    op = Operator.greaterThan;
                    break;
                default:
                    System.err.println("RuleFragOffset constructor: Invalid operator");
            }
            offset = Integer.parseUnsignedInt(details.substring(1));
        }
    }
    @Override
    public boolean matches(Packet packet) {
        TCPProtocol tcp = packet.getProtocol(TCPProtocol.class);
        if (tcp != null) {
            switch (op) {
                case equal:
                    return offset == tcp.data_offset.getAsInt();
                case notEqual:
                    return offset != tcp.data_offset.getAsInt();
                case lessThan:
                    return tcp.data_offset.getAsInt() <= offset;
                case greaterThan:
                    return tcp.data_offset.getAsInt() >= offset;
            }
        }
        return false;
    }
}
