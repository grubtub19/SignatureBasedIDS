import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IPAssembler {
    Map<Integer, Datagram> open_datagrams;

    Packet individual_packet;

    public IPAssembler() {
        open_datagrams = new HashMap<Integer, Datagram>();
    }

    public Datagram getDatagram(int id) {
        return open_datagrams.get(id);
    }

    public boolean hasDatagram(int id) {
        return open_datagrams.get(id) != null;
    }

    /**
     * Add a packet that has only parsed until the end of the IPProtocol header.
     * @param packet The partially parsed packet
     */
    public void addFragment(Packet packet) {
        // If IP, reassemble
        if (packet.hasProtocol(IPProtocol.class)) {

            IPProtocol ip = packet.getProtocol(IPProtocol.class);

            // Check to see if there's already a Datagram open for this IP ID
            Datagram datagram = open_datagrams.get(ip.id.getAsInt());

            // If the packet is null create a new datagram and add this packet
            if (datagram == null) {
                datagram = new Datagram();
                open_datagrams.put(ip.id.getAsInt(), datagram);
            }
            if (!packet.moreFragments()) {
                datagram.last_received = true;
            }
            datagram.addFragment(packet);
        } else {
            individual_packet = packet;
        }
    }

    public ArrayList<Signature> update() {
        ArrayList<Signature> signatures = removeOld();
        Signature assembled = processFinished();
        if (assembled != null) {
            signatures.add(assembled);
        }
        return signatures;
    }

    /**
     * Due to the single-threaded nature of this program, only 1 Datagram can be assembled every update()
     * @return Signature
     */
    private Signature processFinished() {

        // If one of the datagrams is now finished, print it
        if (individual_packet == null) {
            for (Map.Entry<Integer, Datagram> entry : open_datagrams.entrySet()) {
                Datagram datagram = entry.getValue();
                // Remove finished Datagrams
                if (datagram.finished()) {
                    open_datagrams.remove(entry.getKey());
                    datagram.assemblePacket();

                    if (!datagram.assembled_packet.isFiltered()) {
                        // Print if unfiltered
                        //TODO remove this print
                        //System.out.println(datagram.toString());
                        return datagram.getSignature();
                    }
                }
            }
        // If its an individual packet that was added this update, print it and return its signature
        } else if (!individual_packet.isFiltered()) {
            //TODO remove this print
            //System.out.println(individual_packet.toString());
            Signature signature = new Signature(individual_packet);
            individual_packet = null;
            return signature;
        }
        // If either the packet is filtered or no Datagrams have completed
        return null;
    }

    /**
     * Remove all datagrams that have timed-out
     */
    public ArrayList<Signature> removeOld() {
        // Gather the ids of timed-out Datagrams
        ArrayList<Integer> ids = new ArrayList<>();
        for (Map.Entry<Integer, Datagram> entry : open_datagrams.entrySet()) {
            if (entry.getValue().timeout()) {
                ids.add(entry.getKey());
            }
        }
        // Get the signatures of the unfinished datagrams at those ids and remove
        ArrayList<Signature> signatures = new ArrayList<>();
        for (int i : ids) {
            // TODO remove this print line
            //System.out.println("Timeout for IP id: " + i);
            signatures.add(new Signature(open_datagrams.remove(i)));
        }
        return signatures;
    }

    private void printAssembled(Datagram datagram, Packet assembled_packet) {
        System.out.print(datagram.toString());
    }


}
