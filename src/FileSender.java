import java.nio.ByteBuffer;

public class FileSender {
    private PacketSender packet_sender;
    private FileReader packet_reader;

    public FileSender() {
        packet_sender = new PacketSender();
        packet_reader = null;
    }

    public FileSender(PacketSender packet_sender) {
        this.packet_sender = packet_sender;
        packet_reader = null;
    }

    public void sendFile(String filename) {
        packet_reader = new FileReader(filename);

        // While there's another Packet in the file
        while (packet_reader.hasPacket()) {
            // Parse it
            ByteBuffer packet = packet_reader.nextPacket();
            //System.out.println("Sending packet: " + packet.toString());
            // Send it
            packet_sender.sendPacket(packet);
        }
    }
}
