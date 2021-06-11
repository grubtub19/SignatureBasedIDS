public class RuleFlags implements RuleOption {
    public enum Operator {
        and, or, not
    }

    boolean f;
    boolean s;
    boolean r;
    boolean p;
    boolean a;
    boolean u;
    boolean c;
    boolean n;
    boolean o;
    Operator op;

    public RuleFlags(String details) {
        op = Operator.and;
        for (char letter : details.toCharArray()) {
            switch (letter) {
                case 'F':
                    f = true;
                    break;
                case 'S':
                    s = true;
                    break;
                case 'R':
                    r = true;
                    break;
                case 'P':
                    p = true;
                    break;
                case 'A':
                    a = true;
                    break;
                case 'U':
                    u = true;
                    break;
                case 'C':
                    c = true;
                    break;
                case 'N':
                    n = true;
                    break;
                case '0':
                    o = true;
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
        TCPProtocol tcp = packet.getProtocol(TCPProtocol.class);
        if (tcp != null) {
            if (f) {
                if (tcp.fin.getBit(0)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (s) {
                if (tcp.syn.getBit(0)) {
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
                if (tcp.rst.getBit(0)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (p) {
                if (tcp.psh.getBit(0)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (a) {
                if (tcp.ack.getBit(0)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (u) {
                if (tcp.urg.getBit(0)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (c) {
                if (tcp.cwr.getBit(0)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (n) {
                if (tcp.ns.getBit(0)) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                } else if (op == Operator.and) {
                    return false;
                }
            }
            if (o) {
                if (!(tcp.fin.getBit(0) || tcp.syn.getBit(0) || tcp.rst.getBit(0) || tcp.psh.getBit(0)
                    || tcp.ack.getBit(0) || tcp.urg.getBit(0) || tcp.cwr.getBit(0) || tcp.ns.getBit(0))) {
                    if (op == Operator.or) {
                        return true;
                    } else if (op == Operator.not) {
                        return false;
                    }
                }  else if (op == Operator.and) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
