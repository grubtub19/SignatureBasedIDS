import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PacketWriter {
    private FileWriter writer;
    private int line_count;
    private String default_rule_log = "rules_log.txt";

    public PacketWriter(String filename) {
        try {
            System.out.println("Writing all packets to " + filename);
            File file = new File(filename);
            writer = new FileWriter(file, false);
            //System.out.println("Output File: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("PacketWriter(): \n" + e);
        }
    }

    public void write(byte[] bytes) {
        for (byte b : bytes) {
            writeString(String.format("%02X ", b));
            line_count++;
            if (line_count >= 16) {
                writeString("\n");
                line_count = 0;
            }
        }
        writeString("\n\n");
        line_count = 0;
    }

    private void writeString(String string) {
        try {
            writer.write(string);
            writer.flush();
        } catch (IOException e)  {
            System.err.println("PacketWriter.write(): \n" + e);
        }
    }

    public void writeRule(Packet packet, String filename, String message, int sid) {
        File file;
        if (filename != null) {
            file = new File(filename);
        } else {
            file = new File(default_rule_log);
        }
        try {
            FileWriter writer2 = new FileWriter(file, true);
            writer2.write("\nMsg: " + message + "\n");
            writer2.write("SID: " + sid + "\n");
            writer2.write(packet.toString());
            writer2.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e)  {
            System.err.println("PacketWriter.write(): \n" + e);
        }
    }
}
