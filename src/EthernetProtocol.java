import java.nio.ByteBuffer;

public class EthernetProtocol extends Protocol {

    private Field destination_mac_address = new Field("Destination MAC Address", FieldType.MAC, 6 * 8);
    private Field source_mac_address = new Field("Destination MAC Address", FieldType.MAC, 6 * 8);
    private Field ether_type = new Field("EtherType", FieldType.HEX, 2 * 8);
    //private Field crc_checksum = new Field("CRC Checksum", Type.HEX, 4 * 8);

    public EthernetProtocol(Packet packet) {
        super(packet);
        addHeaderFields();
    }

    public EthernetProtocol(Packet packet, EthernetProtocol eth_protocol) {
        super(packet, eth_protocol);
        destination_mac_address = new Field(eth_protocol.destination_mac_address);
        source_mac_address = new Field(eth_protocol.source_mac_address);
        ether_type = new Field(eth_protocol.ether_type);
        addHeaderFields();
    }

    private void addHeaderFields() {
        header_fields.add(destination_mac_address);
        header_fields.add(source_mac_address);
        header_fields.add(ether_type);
        //tail_fields.add(crc_checksum);
    }

    /**
     * Returns a new Protocol depending on the EtherType.
     * @return Protocol or null if the EtherType is not supported.
     */
    public void getNextProtocol(ParseParams params) {
        int code = ether_type.getAsInt();

        switch (code) {
            case 0x0800:
                next_protocol = new IPProtocol(packet);
                break;
            case 0x0806:
                next_protocol = new ARPProtocol(packet);
                break;
            default:
                System.err.println("Ethernet: EtherType is not supported: " + ether_type.toString());
                System.err.println("Code: " + code);
        }
    }

    @Override
    public int getBodyBytes() {
        return 0;
    }

    @Override
    public String toString() {
        return toString("Ethernet", 6);
    }
}
