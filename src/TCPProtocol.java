import java.nio.ByteBuffer;

public class TCPProtocol extends Protocol {
    int byte_length;
    Field source_port = new Field("Source Port", FieldType.INT, 8 * 2);
    Field destination_port = new Field("Source Port", FieldType.INT, 8 * 2);
    Field sequence_number = new Field("Sequence Number", FieldType.INT, 8 * 4);
    Field acknowledgment_number = new Field("Acknowledgment Number", FieldType.INT, 8 * 4);
    Field data_offset = new Field("Data Offset", FieldType.INT, 4);
    Field reserved = new Field("Reserved", FieldType.INT, 3);
    Field ns = new Field("NS", FieldType.BITS, 1);
    Field cwr = new Field("CWR", FieldType.BITS, 1);
    Field ece = new Field("ECE", FieldType.BITS, 1);
    Field urg = new Field("URG", FieldType.BITS, 1);
    Field ack = new Field("ACK", FieldType.BITS, 1);
    Field psh = new Field("PSH", FieldType.BITS, 1);
    Field rst = new Field("RST", FieldType.BITS, 1);
    Field syn = new Field("SYN", FieldType.BITS, 1);
    Field fin = new Field("FIN", FieldType.BITS, 1);
    Field window_size = new Field("Window Size", FieldType.INT, 8 * 2);
    Field checksum = new Field("Checksum", FieldType.HEX, 8 * 2);
    Field urgent_pointer = new Field("Urgent Pointer", FieldType.HEX, 8 * 2);
    VariableField options = new VariableField("Options", FieldType.HEX, data_offset, ProtocolType.tcp);


    public TCPProtocol(Packet packet, int byte_length) {
        super(packet);
        this.byte_length = byte_length;
        addHeaderFields();
    }

    public TCPProtocol(Packet packet, TCPProtocol tcp_protocol) {
        super(packet, tcp_protocol);
        source_port = new Field(tcp_protocol.source_port);
        destination_port = new Field(tcp_protocol.destination_port);
        sequence_number = new Field(tcp_protocol.sequence_number);
        acknowledgment_number = new Field(tcp_protocol.acknowledgment_number);
        data_offset = new Field(tcp_protocol.data_offset);
        reserved = new Field(tcp_protocol.reserved);
        ns = new Field(tcp_protocol.ns);
        cwr = new Field(tcp_protocol.cwr);
        ece = new Field(tcp_protocol.ece);
        urg = new Field(tcp_protocol.urg);
        ack = new Field(tcp_protocol.ack);
        psh = new Field(tcp_protocol.psh);
        rst = new Field(tcp_protocol.rst);
        syn = new Field(tcp_protocol.syn);
        fin = new Field(tcp_protocol.fin);
        window_size = new Field(tcp_protocol.window_size);
        checksum = new Field(tcp_protocol.checksum);
        urgent_pointer = new Field(tcp_protocol.urgent_pointer);
        options = new VariableField(tcp_protocol.options, data_offset);
        addHeaderFields();
    }

    private void addHeaderFields() {
        header_fields.add(source_port);
        header_fields.add(destination_port);
        header_fields.add(sequence_number);
        header_fields.add(acknowledgment_number);
        header_fields.add(data_offset);
        header_fields.add(reserved);
        header_fields.add(ns);
        header_fields.add(cwr);
        header_fields.add(ece);
        header_fields.add(urg);
        header_fields.add(ack);
        header_fields.add(psh);
        header_fields.add(rst);
        header_fields.add(syn);
        header_fields.add(fin);
        header_fields.add(window_size);
        header_fields.add(checksum);
        header_fields.add(urgent_pointer);
        header_fields.add(options);
    }


    @Override
    public void getNextProtocol(ParseParams params) {
        // TCP cannot contain other headers
    }

    public int getBodyBytes() {
        // Length of frame (byte_length) - header (data_offset) = data in bytes (data_bytes)
        return byte_length - 4 * data_offset.getAsInt();
    }

    @Override
    public String toString() {
        return toString("TCP", 4);
    }
}
