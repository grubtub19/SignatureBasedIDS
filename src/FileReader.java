import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileReader {
    private BufferedReader reader;
    private ByteBuffer next_packet;

    public FileReader(String filename) {

        // Open a file from the resources folder with this filename
        File file = new File(filename);

        // Create a BufferedReader for this File
        try {
            reader = new BufferedReader(new java.io.FileReader(file));
        } catch (FileNotFoundException e) {
            System.err.println("FileReader.filesToBytes(): " + e);
            System.exit(-1);
        }
    }

    /**
     * Prepares the next packet and checks if it's empty or we've reached the EOF
     * @return whether the next packet is valid
     */
    public boolean hasPacket() {

        try {
            // While there's more lines and the next packet has not already been read
            while (reader.ready() && next_packet == null) {

                // Read the next Packet
                // If there's no data at this line, it remains null and loops again
                next_packet = nextPacket();
            }
        } catch (IOException e) {
            System.err.println(e);
            return false;
        }

        // Either we reach the EOF or we get have successfully prepared a packet
        return next_packet != null;
    }

    /**
     * Tries to assemble a packet from the next line
     * Consumes the packet and the empty space below it.
     * If the packet itself is empty, it returns a Packet without data.
     * @return
     */
    public ByteBuffer nextPacket() {
        // If we already have a Packet queued up from .hasPacket(), return that and reset next_packet
        if (next_packet != null) {
            ByteBuffer temp = next_packet;
            next_packet = null;
            return temp;
        }
        String line;
        String hex_string = "";
        try {
            // While there's another line to the current packet (not empty or EOF)
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Split by spaces
                hex_string += line;
            }

        } catch (IOException e) {
            System.err.println("FileReader.nextPackets(): " + e);
            System.exit(-1);
        }
        // If there was no data starting at the current line in the file, return null
        if (hex_string.isEmpty()) {
            return null;
        } else {
            // Remove all spaces
            hex_string = hex_string.replace(" ", "");

            // Convert hexidecimal to Bytes and create a new Packet
            try {
                return ByteBuffer.wrap(Hex.decodeHex(hex_string.toCharArray()));
            } catch (DecoderException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
