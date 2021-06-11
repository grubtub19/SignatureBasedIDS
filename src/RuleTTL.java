public class RuleTTL implements RuleOption {
    public enum Operator {
        lessThan, lessThanEqual, equal, greaterThan, greaterThanEqual, range
    }
    Operator op;
    short ttl1 = -1;
    short ttl2 = -1;

    public RuleTTL(String details) {
        if(Character.isDigit(details.charAt(0))) {
            ttl1 = (short) Character.getNumericValue(details.charAt(0));
            op = Operator.range;
            // Get the upper limit if there is one
            if (details.length() == 3) {
                ttl2 = (short) Character.getNumericValue(details.charAt(2));
            }
        } else {
            switch (details.charAt(0)) {
                case '-':
                    op = Operator.range;
                    ttl2 = (short) Character.getNumericValue(details.charAt(1));
                    break;
                case '<':
                    if (details.charAt(1) == '=') {
                        op = Operator.lessThanEqual;
                        ttl1 = (short) Character.getNumericValue(details.charAt(2));
                    } else {
                        op = Operator.lessThan;
                        ttl1 = (short) Character.getNumericValue(details.charAt(1));
                    }
                    break;
                case '>':
                    if (details.charAt(1) == '=') {
                        op = Operator.greaterThanEqual;
                        ttl1 = (short) Character.getNumericValue(details.charAt(2));
                    } else {
                        op = Operator.greaterThan;
                        ttl1 = (short) Character.getNumericValue(details.charAt(1));
                    }
                    break;
                case '=':
                    op = Operator.equal;
                    ttl1 = (short) Character.getNumericValue(details.charAt(1));
                    break;
                default:
                    System.err.println("RuleTTL constructior: invalid operator");
            }
        }
    }

    @Override
    public boolean matches(Packet packet) {
        IPProtocol ip = packet.getProtocol(IPProtocol.class);
        if (ip == null) {
            return false;
        }
        int ttl = ip.time_to_live.getAsInt();
        switch (op) {
            case lessThan:
                return ttl < ttl1;
            case lessThanEqual:
                return ttl <= ttl1;
            case equal:
                return ttl == ttl1;
            case greaterThan:
                return ttl > ttl1;
            case greaterThanEqual:
                return ttl >= ttl1;
            case range:
                if (ttl1 != -1 && ttl2 != -1) {
                    return ttl >= ttl1 && ttl <= ttl2;
                }
                if (ttl1 != -1) {
                    return ttl >= ttl1 && ttl <= 225;
                }
                return ttl >= 0 && ttl <= ttl2;
            default:
                System.err.println("RULETTL.matches(): operation not valid");
                return false;
        }
    }
}
