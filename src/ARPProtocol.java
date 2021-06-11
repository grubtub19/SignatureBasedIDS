public class ARPProtocol extends Protocol {
    Field hardware_type = new Field("Hardware Type", FieldType.INT, 8 * 2);
    Field protocol_type = new Field("Protocol Type", FieldType.HEX, 8 * 2);
    Field hardware_length = new Field("Hardware Length", FieldType.INT, 8);
    Field protocol_address_length = new Field("Protocol Address Length", FieldType.INT, 8);
    Field operation = new Field("Operation", FieldType.INT, 8 * 2);
    Field sender_hardware_address = new Field("Sender Hardware Address", FieldType.MAC, 8 * 6);
    Field sender_protocol_address = new Field("Sender Protocol Address", FieldType.IP, 8 * 4);
    Field target_hardware_address = new Field("Target Hardware Address", FieldType.MAC, 8 * 6);
    Field target_protocol_address = new Field("Target Protocol Address", FieldType.IP, 8 * 4);


    public ARPProtocol(Packet packet) {
        super(packet);
        addHeaderFields();
    }

    public ARPProtocol(Packet packet, ARPProtocol arp_protocol) {
        super(packet, arp_protocol);

        hardware_type = new Field(arp_protocol.hardware_type);
        protocol_type = new Field(arp_protocol.protocol_type);
        hardware_length = new Field(arp_protocol.hardware_length);
        protocol_address_length = new Field(arp_protocol.protocol_address_length);
        operation = new Field(arp_protocol.operation);
        sender_hardware_address = new Field(arp_protocol.sender_hardware_address);
        sender_protocol_address = new Field(arp_protocol.sender_protocol_address);
        target_hardware_address = new Field(arp_protocol.target_hardware_address);
        target_protocol_address = new Field(arp_protocol.target_protocol_address);

        addHeaderFields();
    }

    private void addHeaderFields() {
        header_fields.add(hardware_type);
        header_fields.add(protocol_type);
        header_fields.add(hardware_length);
        header_fields.add(protocol_address_length);
        header_fields.add(operation);
        header_fields.add(sender_hardware_address);
        header_fields.add(sender_protocol_address);
        header_fields.add(target_hardware_address);
        header_fields.add(target_protocol_address);
    }

    @Override
    public void getNextProtocol(ParseParams params) {
        // ARP packets cannot hold more headers
    }

    @Override
    public int getBodyBytes() {
        // ARP packets have no payload
        return 0;
    }

    @Override
    public String toString() {
        return toString("ARP", 4);
    }
}
