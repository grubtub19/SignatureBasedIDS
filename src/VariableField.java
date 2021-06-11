/**
 * Used for the Options fields. Fortunately, only IP and TCP use this and they both use the same units for defining
 * the length of the Options field.
 */
public class VariableField extends Field {

    public Field length_field;
    public ProtocolType protocol;

    public VariableField(String name, FieldType field_type, Field length_field, ProtocolType protocol) {
        super(name, field_type, -1);
        this.length_field = length_field;
        this.protocol = protocol;
    }

    public VariableField(VariableField field, Field length_field) {
        super(field);
        this.length_field = length_field;
        this.protocol = field.protocol;

        // For development purposes. I should delete later if this never occurs
        if (field.length_field == length_field) {
            System.err.println("VariableField: length_field is a pointer to a reference");
        }
    }

    @Override
    public void parse(ParseParams params) {
        // 32-bit word
        int header_bits = 32 * length_field.getAsInt();

        // IP and TCP header is 160 bits without options
        num_bits = header_bits - 160;

        bytes = new byte[((num_bits + 8 - 1) / 8)];
        super.parse(params);
    }
}
