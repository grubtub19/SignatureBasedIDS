import java.nio.ByteBuffer;

public class ICMPProtocol extends Protocol {
    int byte_length;
    Field type = new Field("Type", FieldType.HEX, 8);
    Field code = new Field("Code", FieldType.HEX, 8);
    Field checksum = new Field("Checksum", FieldType.HEX, 8 * 2);
    Field rest_of_header = new Field("Rest of Header", FieldType.BITS, 8 * 4);

    public ICMPProtocol(Packet packet, int byte_length) {
        super(packet);
        this.byte_length = byte_length;
        addHeaderFields();
    }

    public ICMPProtocol(Packet packet, ICMPProtocol icmp_protocol) {
        super(packet, icmp_protocol);
        byte_length = icmp_protocol.byte_length;
        type = new Field(icmp_protocol.type);
        code = new Field(icmp_protocol.code);
        checksum = new Field(icmp_protocol.checksum);
        rest_of_header = new Field(icmp_protocol.rest_of_header);
        addHeaderFields();
    }

    private void addHeaderFields() {
        header_fields.add(type);
        header_fields.add(code);
        header_fields.add(checksum);
        header_fields.add(rest_of_header);
    }

    @Override
    public void getNextProtocol(ParseParams params) {
        // ICMP cannot hold other headers
    }

    public int getBodyBytes() {
        // entire frame (length) - header (8) = data in bytes (data_bytes)
        return byte_length - 8;
    }

    @Override
    public String toString() {
        return toString("ICMP", 4);
    }
}
