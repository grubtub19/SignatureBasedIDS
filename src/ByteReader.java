import java.util.ArrayList;

public abstract class ByteReader {
    ArrayList<Field> fields;

    public ByteReader(ArrayList<Field> fields) {
        this.fields = fields;
    }

    public byte[] bytes;

    /**
     * The number of bytes all the fields take up
     * @return
     */
    public int getNumBytes() {
        int bits = 0;
        for (Field field: fields) {
            bits += field.num_bits;
        }
        assert bits % 8 == 0;
        return bits / 8;
    }

    public int byteToInt() {
        int result = 0;
        for (int i = bytes.length; i > 0; i--) {
            int shift = (bytes.length - i) * 8;
            result = result | ((bytes[i] & 0xff) << shift);
        }
        return result;
    }
}