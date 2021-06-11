import java.lang.reflect.AnnotatedElement;

public enum ProtocolType {
    eth, arp, ip, icmp, tcp, udp;

    public Class<? extends Protocol> getAsClass() {
        switch (this) {
            case eth:
                return EthernetProtocol.class;
            case arp:
                return ARPProtocol.class;
            case ip:
                return IPProtocol.class;
            case icmp:
                return ICMPProtocol.class;
            case tcp:
                return TCPProtocol.class;
            case udp:
                return UDPProtocol.class;
            default:
                System.err.println("Option Protocol Type invalid");
                return null;
        }
    }

    public static ProtocolType getFromString(String protocol_str) {
        switch (protocol_str) {
            case "ip":
                return ProtocolType.ip;
            case "arp":
                return ProtocolType.arp;
            case "tcp":
                return ProtocolType.tcp;
            case "udp":
                return ProtocolType.udp;
            case "icmp":
                return ProtocolType.icmp;
            default:
                System.err.println("ProtocolType.getFromString(): unknown protocol");
                return null;
        }
    }
}