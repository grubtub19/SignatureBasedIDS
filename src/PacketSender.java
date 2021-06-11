import java.nio.ByteBuffer;

public class PacketSender {

    public PacketSender() {

    }

    public void sendPacket(ByteBuffer bytes) {
        if (bytes == null) {
            System.out.println("PacketSender.sendPacket(): Packet has no data.");
        }
        //System.out.println("Sending packet");
        if (!Driver.get().driver.sendPacket(bytes.array())) {
            System.err.println("SimplePacketDriver.sendPacket() failed for a reason outside my control. Typically happens when packet is large.");
            if(bytes.array() == null) {
                System.err.println("My fault"); //This never happens
            }
        }
    }
}
