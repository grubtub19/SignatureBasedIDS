import java.util.ArrayList;

public abstract class Protocol {
    protected ArrayList<Field> header_fields;
    protected Packet packet;
    protected Protocol prev_protocol;
    protected Protocol next_protocol;
    protected ArrayList<Field> tail_fields;

    public Protocol(Packet packet) {
        this.packet = packet;
        header_fields = new ArrayList<Field>();
        tail_fields = new ArrayList<Field>();
    }

    public Protocol(Packet packet, Protocol protocol) {
        this.packet = packet;
        header_fields = new ArrayList<Field>();
        tail_fields = new ArrayList<Field>();
    }

    public void parseHeaderFields(ParseParams params) {
        parseFields(header_fields, params);
    }

    public void parseTailFields(ParseParams params) {
        parseFields(tail_fields, params);
    }

    public void parseFields(ArrayList<Field> fields, ParseParams params) {
        for (Field field : fields) {
            if (!(params.finished && field.num_bits > 0)) {
                field.parse(params);
            } else {
                System.err.println("Malformed Packet: Not enough bytes to parse header");
            }
        }
    }

    /**
     * Recursively parse all header fields, data, and tail fields.
     * @param params
     * @return data as a String
     */
    protected byte[] parse(ParseParams params) {
        parseHeaderFields(params);
        return parseNext(params);
    }

    protected byte[] parseNext(ParseParams params) {
        byte[] result;

        // If the buffer we're parsing is consumed, don't try to read anything else (header, body, or tail)
        if (!params.finished) {

            // Set the next protocol
            getNextProtocol(params);

            // If there is an encapsulated next_protocol
            if (next_protocol != null) {

                // Link its previous to be this protocol
                next_protocol.prev_protocol = this;

                // Parse it
                result = next_protocol.parse(params);

            } else {
                // Parse the body
                result = parseBody(params);
            }

            // Parse the Tail fields
            parseTailFields(params);
            return result;
        }
        return new byte[0];
    }

    /**
     * A hook to add functionality after we parse this protocol's header
     * If we intend to parse another header, instantiate next_protocol
     * @return the length of the body in bytes if there's no next header, -1 if there is
     */
    public abstract void getNextProtocol(ParseParams params);

    public abstract int getBodyBytes();

    public byte[] parseBody(ParseParams params) {
        int body_bytes;
        if (!packet.assembled) {
            body_bytes = getBodyBytes();
        } else {
            body_bytes = params.buffer.remaining();
        }
        if (body_bytes > 0) {
            byte[] array = new byte[body_bytes];
            if (params.original_start_index != 0) {
                System.err.println("Packet header was not byte aligned");
            }
            array[0] = params.b;
            for (int i = 1; i < body_bytes; i++) {
                if (params.buffer.hasRemaining()) {
                    array[i] = params.buffer.get();
                }
            }
            return array;
        }
        return new byte[0];
    }

    public abstract String toString();

    public String toString(String protocol_name, int byte_width) {
        StringBuilder result = new StringBuilder();
        result.append(protocol_name + ":\n");

        result = fieldsToString(header_fields, result, byte_width);
        result.append("\n");

        //if (next_protocol != null) {
        //    result.append(next_protocol.toString());
        //}

        if(!tail_fields.isEmpty()) {
            result.append("\n" + protocol_name + " Tail:\n");
            result = fieldsToString(tail_fields, result, byte_width);
        }
        return result.toString();
    }

    public StringBuilder fieldsToString(ArrayList<Field> fields, StringBuilder result, int byte_width) {

        StringBuilder body = new StringBuilder();
        boolean first_row_filled = false;
        int top_width;
        // The number of bits in a single row
        int bit_width = byte_width * 8;

        // Generate the body of the packet first so we know how big the surrounding border needs to be
        String line = "";
        String last_line = "";
        for (Field field : fields) {
            if (field.bytes.length == 0) continue;
            line += field.tableString(byte_width, last_line.length() / 2);

            int index_of_new_line = line.lastIndexOf("\n");
            if (index_of_new_line != -1) {
                last_line = line.substring(line.lastIndexOf("\n"));
            } else {
                last_line = line;
            }
            // If we reached the max width
            // (bit_width * 2 - 1) because bits have spaces between each other but not on the ends
            if (last_line.length() >= bit_width * 2 - 1) {
                body.append("+");
                body.append(line);
                body.append("+\n");
                body.append("+-".repeat(byte_width * 8));
                body.append("+\n");
                line = "";
                last_line = "";
                first_row_filled = true;
            } else {
                line += "|";
            }
        }

        // Calculate how long the top and bottom borders should be
        // If the top line doesn't reach the full width
        if (!first_row_filled && !line.isEmpty()) {

            // The top width is determined by the length of this line
            top_width = line.length() / 2;
        } else {
            top_width = bit_width;
        }
        // Add byte numbers on top
        for (int i = 0; i < top_width; i++) {
            if(i % 8 == 0) {
                result.append(" ");
                result.append(i / 8);
            } else {
                result.append("  ");
            }
        }
        // Add bit numbers on top
        result.append("\n");
        for (int i = 0; i < top_width; i++) {
            result.append(" ");
            result.append(i % 8);
        }
        // Add the top border
        result.append("\n");
        result.append("+-".repeat(top_width));
        result.append("+\n");

        // Add the body
        result.append(body);

        // If the bottom line isn't full width and still contains stuff, we have to add it's border
        // (If the bottom line was full width, the border is already added)
        if(!line.isEmpty()) {
            // Add the last line
            result.append("+");
            result.append(line);
            // Replace the | at the end of the line with a +
            result.replace(result.length() - 1, result.length(), "+\n");
            // Add the bottom border
            result.append("+-".repeat(line.length() / 2));
            result.append("+\n");
        }
        return result;
    }
}
