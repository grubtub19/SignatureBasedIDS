import java.util.Arrays;

public class IPProtocol extends Protocol {
    public Field version = new Field("Version", FieldType.INT, 4);
    public Field ihl = new Field("IHL", FieldType.INT, 4);
    public Field dscp = new Field("DSCP", FieldType.HEX, 6);
    public Field ecn = new Field("ECN", FieldType.BITS, 2);
    public Field total_length = new Field("Total Length", FieldType.INT, 2 * 8);
    public Field id = new Field("Identification", FieldType.HEX, 2 * 8);
    public Field flags = new Field("Flags", FieldType.BITS, 3);
    public Field fragment_offset = new Field("Fragment Offset", FieldType.INT, 13);
    public Field time_to_live = new Field("Time To Live", FieldType.INT, 8);
    public Field protocol = new Field("Protocol", FieldType.HEX, 8);
    public Field header_checksum = new Field("Header Checksum", FieldType.HEX, 8 * 2);
    public Field source_ip_address = new Field("Source IP Address", FieldType.IP, 4 * 8);
    public Field destination_ip_address = new Field("Destination IP Address", FieldType.IP, 4 * 8);
    public VariableField options = new VariableField("Options", FieldType.HEX, ihl, ProtocolType.tcp);

    public IPProtocol(Packet packet) {
        super(packet);
        addHeaderFields();
    }

    public IPProtocol(Packet packet, IPProtocol ip_protocol) {
        super(packet, ip_protocol);
        version = new Field(ip_protocol.version);
        ihl = new Field(ip_protocol.ihl);
        dscp = new Field(ip_protocol.dscp);
        ecn = new Field(ip_protocol.ecn);
        total_length = new Field(ip_protocol.total_length);
        id = new Field(ip_protocol.id);
        flags = new Field(ip_protocol.flags);
        fragment_offset = new Field(ip_protocol.fragment_offset);
        time_to_live = new Field(ip_protocol.time_to_live);
        protocol = new Field(ip_protocol.protocol);
        header_checksum = new Field(ip_protocol.header_checksum);
        source_ip_address = new Field(ip_protocol.source_ip_address);
        destination_ip_address = new Field(ip_protocol.destination_ip_address);
        options = new VariableField(ip_protocol.options, ihl);
        addHeaderFields();
    }

    private void addHeaderFields() {
        header_fields.add(version);
        header_fields.add(ihl);
        header_fields.add(dscp);
        header_fields.add(ecn);
        header_fields.add(total_length);
        header_fields.add(id);
        header_fields.add(flags);
        header_fields.add(fragment_offset);
        header_fields.add(time_to_live);
        header_fields.add(protocol);
        header_fields.add(header_checksum);
        header_fields.add(source_ip_address);
        header_fields.add(destination_ip_address);
        header_fields.add(options);
    }

    public boolean moreFragments() {
        boolean more = flags.getBit(2);
        return more;
    }

    // Either (there's more fragments) or (this is the last fragment but it has an offset) or (previous fragments exist)
    // or (we're parsing a reassembled packet)
    public boolean isFragment() {
        return !packet.assembled &&
                (moreFragments() || fragment_offset.getAsInt() != 0 || Driver.get().assembler.hasDatagram(id.getAsInt()));
    }

    @Override
    public void getNextProtocol(ParseParams params) {


        if (!doCheckSum(params)) {
            // TODO add this information to some packet status class
            System.err.println("IP Checksum invalid.");
        }


        // If this a fragment, don't try to parse the encapsulated header
        if (isFragment()) {

            // Explicitly set the next_protocol to null for clarity
            next_protocol = null;

        // If this is a standalone IP packet, set the next_protocol
        } else {
            next_protocol = getNext(getBodyBytes());
        }
    }

    public Protocol getNext(int body_bytes) {
        int code = protocol.getAsInt();
        switch (code) {
            case 0x01:
                return new ICMPProtocol(packet, body_bytes);
            case 0x06:
                return new TCPProtocol(packet, body_bytes);
            case 0x11:
                return new UDPProtocol(packet);
            default:
                System.err.println("IPAssembler: Protocol is not supported: " + code);
                System.err.println("Protocol: " + code);
                return null;
        }
    }

    public ProtocolType nextProtocol() {
        int code = protocol.getAsInt();
        switch (code) {
            case 0x01:
                return ProtocolType.icmp;
            case 0x06:
                return ProtocolType.tcp;
            case 0x11:
                return ProtocolType.udp;
            default:
                System.err.println("IPProtocol.nextProtocol(): Protocol is not supported: " + code);
                return null;
        }
    }

    public int computeChecksum(byte[] data) {
        int checksum;
        int sum = 0;
        for(int i = 0 ; i < data.length; i+=2) {
            // checksum field is 0'd out
            if ( i == 10 ) {
                continue;
            }
            int word_value = (data[i] << 8 & 0xffff) + (data[i + 1] & 0xff);
            sum += word_value;
        }
        int carry = (sum >> 16) & 0xff;
        sum = carry + (sum & 0xffff);
        int total = (~sum) & 0xffff;
        return total;
    }

    public boolean doCheckSum(ParseParams params) {
        int header_length_bytes = ihl.getAsInt() * 4;

        int header_end_exclusive = params.buffer.position() - 1;
        int header_start_inclusive = header_end_exclusive - header_length_bytes;

        byte[] header_bytes = Arrays.copyOfRange(params.buffer.array(), header_start_inclusive, header_end_exclusive);

        return header_checksum.getAsInt() == computeChecksum(header_bytes);
    }

    @Override
    public int getBodyBytes() {
        return total_length.getAsInt() - ihl.getAsInt() * 4;
    }

    @Override
    public String toString() {
        return toString("IP", 4);
    }
}
