import java.util.ArrayList;
import java.util.Scanner;

public class RuleContent implements RuleOption {

    boolean not;
    byte[] content;

    public RuleContent(String details) {
        boolean binary = false;
        if (details.charAt(0) =='!') {
            not = true;
            details = details.substring(1);
        }
        // Remove leading and trailing "
        details = details.substring(1, details.length() - 1);

        int i = 0;
        char[] arr = details.toCharArray();
        ArrayList<Byte> assemble_content = new ArrayList<>();
        while (i < arr.length) {
            // Inverse the binary boolean if we reach '|'
            if(arr[i] == '|') {
                binary = !binary;
                i++;
                if (i >= arr.length) {
                    break;
                }
            }
            // Skip the space between binary characters
            if(binary && arr[i] == ' ') {
                i++;
            }
            // Binary (hex) always comes in pairs
            if (binary) {
                String hex = "";
                int firstDigit = Character.digit(arr[i], 16);
                int secondDigit = Character.digit(arr[i + 1], 16);
                //TODO fix unsigned?
                assemble_content.add((byte) ((firstDigit << 4) + secondDigit));
                i += 2;
            // String content goes until either the end or a '|'
            } else {
                 int end = details.indexOf('|', i);
                 if (end == -1) {
                     end = details.length();
                 }
                 String str_content = details.substring(i, end);
                 byte[] bytes = str_content.getBytes();
                 for (byte b : bytes) {
                     assemble_content.add(b);
                 }
                 i = end;
            }
        }

        // Convert from ArrayList<Byte> (assemble_content) to byte[] (content)
        Byte[] obj_bytes = assemble_content.toArray(new Byte[assemble_content.size()]);
        content = new byte[obj_bytes.length];
        for (i = 0; i < obj_bytes.length; i++) {
            content[i] = obj_bytes[i];
        }
    }
    @Override
    public boolean matches(Packet packet) {
        return indexOf(packet.body, content) != -1;
    }

    /**
     * Boyer Moore algorithm
     * @param haystack
     * @param needle
     * @return
     */
    public static int indexOf(byte[] haystack, byte[] needle) {
        if (needle.length == 0) {
            return 0;
        }
        int charTable[] = makeCharTable(needle);
        int offsetTable[] = makeOffsetTable(needle);
        for (int i = needle.length - 1, j; i < haystack.length;) {
            for (j = needle.length - 1; needle[j] == haystack[i]; --i, --j) {
                if (j == 0) {
                    return i;
                }
            }
            // i += needle.length - j; // For naive method
            i += Math.max(offsetTable[needle.length - 1 - j], charTable[haystack[i]]);
        }
        return -1;
    }

    /**
     * Makes the jump table based on the mismatched character information.
     */
    private static int[] makeCharTable(byte[] needle) {
        final int ALPHABET_SIZE = Character.MAX_VALUE + 1; // 65536
        int[] table = new int[ALPHABET_SIZE];
        for (int i = 0; i < table.length; ++i) {
            table[i] = needle.length;
        }
        for (int i = 0; i < needle.length - 2; ++i) {
            table[needle[i]] = needle.length - 1 - i;
        }
        return table;
    }

    /**
     * Makes the jump table based on the scan offset which mismatch occurs.
     * (bad character rule).
     */
    private static int[] makeOffsetTable(byte[] needle) {
        int[] table = new int[needle.length];
        int lastPrefixPosition = needle.length;
        for (int i = needle.length; i > 0; --i) {
            if (isPrefix(needle, i)) {
                lastPrefixPosition = i;
            }
            table[needle.length - i] = lastPrefixPosition - i + needle.length;
        }
        for (int i = 0; i < needle.length - 1; ++i) {
            int slen = suffixLength(needle, i);
            table[slen] = needle.length - 1 - i + slen;
        }
        return table;
    }

    /**
     * Is needle[p:end] a prefix of needle?
     */
    private static boolean isPrefix(byte[] needle, int p) {
        for (int i = p, j = 0; i < needle.length; ++i, ++j) {
            if (needle[i] != needle[j]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the maximum length of the substring ends at p and is a suffix.
     * (good suffix rule)
     */
    private static int suffixLength(byte[] needle, int p) {
        int len = 0;
        for (int i = p, j = needle.length - 1;
             i >= 0 && needle[i] == needle[j]; --i, --j) {
            len += 1;
        }
        return len;
    }
}
