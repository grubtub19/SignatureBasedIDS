import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

public class Field {
    public String name;
    public int num_bits;
    byte[] bytes;
    public FieldType type;

    public Field(String name, FieldType field_type, int num_bits) {
        this.name = name;
        this.num_bits = num_bits;
        if (num_bits >= 0) {
            // do integer division rounding up to reserve the correct # of bytes
            bytes = new byte[((num_bits + 8 - 1) / 8)];
        } else {
            bytes = new byte[0];
        }

        this.type = field_type;
    }

    /**
     * Copy constructor
     * @param field
     */
    public Field(Field field) {
        name = field.name;
        num_bits = field.num_bits;

        bytes = new byte[field.bytes.length];
        System.arraycopy(field.bytes, 0, bytes, 0, field.bytes.length);

        type = field.type;
    }

    public void parse(byte b, int bit_counter) {
        // Go through bytes
        for (int i = 0; i < num_bits / 8; i++) {

        }
    }

    /**
     * Returns a byte with a number of 1's on the right side (ex 3 = 00000111)
     * Java uses signed bytes, so um_ones = 8 results in 255 and is changed to -1 when cast (11111111)
     * @param num_ones
     * @return
     */
    public static byte onesOnRight(int num_ones) {
        double answer = (double) (Math.pow(2, num_ones) - 1);
        return (byte) answer;
    }

    public void parse(ParseParams params) {
        int trans_i = 0;
        int remaining_bits_wanted = num_bits;

        while (remaining_bits_wanted != 0) {

            // The number of bits that we will consume from the byte b
            int bits_available = 8 - params.original_start_index;
            int more_bits_wanted = remaining_bits_wanted % 8;

            // Check if more_bits_wanted is 0 because it's lined up or because we actually don't want anymore
            if (more_bits_wanted == 0 && remaining_bits_wanted != 0) {
                more_bits_wanted = 8;
            }

            int bits_to_transfer = Math.min(bits_available, more_bits_wanted);

            // Zero out previously used bits
            byte clear_byte = (byte) (params.b & onesOnRight(bits_available));

            // The index where our bits will start in our byte[] "bytes"
            int transfer_start_index = 8 - (more_bits_wanted);

            // How many places to the left we need to shift
            int shift_left = params.original_start_index - transfer_start_index;

            // Shift right or left depending
            if (shift_left > 0) {
                // Bitwise or of the cleared, shifted original byte and whatever is in the transfer's byte
                bytes[trans_i] = (byte) (bytes[trans_i] | (clear_byte << shift_left));

            } else {
                // We are doing all math unsigned, so we must use >>>
                int woo = ((clear_byte & 0xff) >>> -shift_left);
                int wow = (bytes[trans_i] | woo);
                bytes[trans_i] = (byte) wow;
            }
            params.original_start_index += bits_to_transfer;
            if (params.original_start_index > 8) {
                System.err.println("The start index should always hit 8 exactly");
            // If we consumed the original byte
            } else if (params.original_start_index == 8) {
                params.original_start_index = 0;
                //System.out.println("Remaining: " + params.packet.remaining());
                if(params.buffer.hasRemaining()) {
                    params.b = params.buffer.get();
                } else {
                    params.finished = true;
                    return;
                }
            }

            transfer_start_index += bits_to_transfer;
            // Increment to the next transfer byte if the current one has finished
            if (transfer_start_index > 8) {
                System.err.println("The start index should always hit 8 exactly");
            } else if (transfer_start_index == 8) {
                //transfer_start_index = 0;
                trans_i += 1;
            }

            remaining_bits_wanted -= bits_to_transfer;
            // Increment to the next original byte if the current one has finished
            if (remaining_bits_wanted < 0) {
                System.err.println("We shouldn't go over the number of bits wanted");
                remaining_bits_wanted = 0;
            }
        }
    }

    public boolean takesBytes() {
        return num_bits % 8 == 0;
    }

    public String toString() {
        switch (type) {
            case BITS:
                return toBitString(bytes);
            case HEX:
                return "0x" + Hex.encodeHexString(bytes);
            case INT:
                return Integer.toUnsignedString(getAsInt(bytes));
            case IP:
                return getAsIP(bytes);
            case MAC:
                return getAsMAC(bytes);
            default:
                System.err.println("Incorrect Type");
                return "ERROR";
        }
    }

    public String tableString(int byte_width, int curr_bits) {
        String result = "";
        int line_bytes;
        for (int i = 0; i < bytes.length; i += line_bytes) {
            if (i > 0) {
                result += "+\n" + "+-".repeat(byte_width * 8) +
                        "+\n+";
            }
            String line = "";
            byte[] sub_bytes;
            // The second index (in bytes) for reading from bytes
            // bytes.length is for the last row, byte_width + i is for rows that don't yet take up all the bytes
            int i2 = Math.min(bytes.length, (byte_width - curr_bits / 8) + i);
            line_bytes = i2 - i;
            // The number of bits of space we should take up
            int line_bits = Math.min(byte_width * 8 - curr_bits, num_bits - i * 8);
            // The subsection of bytes to read from
            sub_bytes = Arrays.copyOfRange(bytes, i, i2);

            switch (type) {
                case BITS:
                    line = toBitString(sub_bytes);
                    break;
                case HEX:
                    line = "0x" + Hex.encodeHexString(sub_bytes);
                    break;
                case INT:
                    line = Integer.toUnsignedString(getAsInt(sub_bytes));
                    break;
                case IP:
                    line = getAsIP(sub_bytes);
                    break;
                case MAC:
                    line = getAsMAC(sub_bytes);
                    break;
                default:
                    System.err.println("Incorrect Type");
                    line = "ERROR";
            }
            int total_space = line_bits * 2 - 1;
            result += center(line, total_space);
            curr_bits = 0;
        }
        return result;
    }

    public String getAsMAC() {
        return getAsMAC(bytes);
    }

    private String getAsMAC(byte[] bytes) {
        String result = "";
        for (byte b : bytes) {
            result += Integer.toHexString(unsignedByteToInt(b)) + ":";
        }
        return result.substring(0, result.length() - 1);
    }

    public String getAsIP() {
        return getAsIP(bytes);
    }

    private String getAsIP(byte[] bytes) {
        String result = "";
        for (byte b : bytes) {
            result += unsignedByteToInt(b) + ".";
        }
        return result.substring(0, result.length() - 1);
    }

    public int getAsInt() {
        return getAsInt(bytes);
    }

    public int getAsInt(byte[] bytes) {
        int result = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            int shift_left = ((bytes.length - 1) - i) * 8;
            int integ = unsignedByteToInt(bytes[i]);
            result = result | integ << shift_left;
        }
        return result;
    }

    public boolean getBit(int bit_num) {
        if (bit_num < num_bits) {
            int bit_i = num_bits - 1 - bit_num;
            int byte_i = bit_i / 8;
            byte b = bytes[byte_i];
            return ((b >> bit_i) & 1) == 1;
        } else {
            System.err.println("bit index is out of range");
            return false;
        }
    }

    /**
     * Implicit cast to int
     * @param b
     * @return
     */
    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Construct a String where bits are spaced by 1 (ex: "1 0 1 1 1 0 0 1")
     * @param b
     * @return
     */
    public String toBitString(byte[] b) {
        final char[] bits = new char[num_bits * 2 - 1];
        for(int i = 0; i < b.length; i++) {
            final byte byteval = b[i];
            int mask = 0x1;

            // bits to read - 1
            int bits_to_read_index = Math.min(7, num_bits - i - 1);
            for(int j = bits_to_read_index; j >= 0; j--) {
                final int bitval = byteval & mask;
                if(bitval == 0) {
                    bits[i * 16 + j * 2] = '0';
                } else {
                    bits[i * 16 + j * 2] = '1';
                }
                if (i * 16 + j * 2 + 1 < bits.length) {
                    bits[i * 16 + j * 2 + 1] = ' ';
                }
                mask <<= 1;
            }
        }

        return String.valueOf(bits);
    }

    public static String center(String text, int len){
        if (len <= text.length())
            return text.substring(0, len);
        int before = (len - text.length())/2;
        if (before == 0)
            return String.format("%-" + len + "s", text);
        int rest = len - before;
        return String.format("%" + before + "s%-" + rest + "s", "", text);
    }
}
