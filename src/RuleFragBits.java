public class RuleFragBits implements RuleOption {
    public enum Operator {
        and, or, not
    }

    boolean m;
    boolean d;
    boolean r;
    Operator op;

    public RuleFragBits(String details) {
        op = Operator.and;
        for (char c : details.toCharArray()) {
            switch (c) {
                case 'M':
                    m = true;
                    break;
                case 'D':
                    d = true;
                    break;
                case 'R':
                    r = true;
                    break;
                case '+':
                    op = Operator.and;
                    break;
                case '*':
                    op = Operator.or;
                    break;
                case '!':
                    op = Operator.not;
                    break;
                default:
                    System.err.println("RuleFragBits constructor: Invalid keyword/modifier");
            }
        }
    }
    @Override
    public boolean matches(Packet packet) {
        IPProtocol ip = packet.getProtocol(IPProtocol.class);
        if (ip != null) {
            if (m) {
                if (ip.flags.getBit(2)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (d) {
                if (ip.flags.getBit(1)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (r) {
                if (ip.flags.getBit(0)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
