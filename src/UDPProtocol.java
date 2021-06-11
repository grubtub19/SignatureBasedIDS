import java.nio.ByteBuffer;

public class UDPProtocol extends Protocol {
    Field source_port = new Field("Source Port", FieldType.INT, 8 * 2);
    Field destination_port = new Field("Destination Port", FieldType.INT, 8 * 2);
    Field length = new Field("Length", FieldType.INT, 8 * 2);
    Field checksum = new Field("Checksum", FieldType.HEX, 8 * 2);

    public UDPProtocol(Packet packet) {
        super(packet);
        addHeaderFields();
    }

    public UDPProtocol(Packet packet, UDPProtocol udp_protocol) {
        super(packet, udp_protocol);
        source_port = new Field(udp_protocol.source_port);
        destination_port = new Field(udp_protocol.destination_port);
        length = new Field(udp_protocol.length);
        checksum = new Field(udp_protocol.checksum);
        addHeaderFields();
    }

    private void addHeaderFields() {
        header_fields.add(source_port);
        header_fields.add(destination_port);
        header_fields.add(length);
        header_fields.add(checksum);
    }

    @Override
    public void getNextProtocol(ParseParams params) {
        // UDP cannot contain other headers
    }

    public int getBodyBytes() {
        // length of entire frame (length) - length of header (8) = data in bytes (data_bytes)
        return length.getAsInt() - 8;
    }

    @Override
    public String toString() {
        return null;
    }
}
