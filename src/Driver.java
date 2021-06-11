import picocli.CommandLine;

import java.util.ArrayList;

public class Driver {
    private static Driver instance = new Driver();
    public SimplePacketDriver driver;
    public Options options;
    public IPAssembler assembler;
    public PacketSniffer packet_sniffer;
    public RuleChecker rule_checker;
    PacketWriter writer;

    public Driver() {

    }

    public void init(String[] args) {
        driver = new SimplePacketDriver();
        //Get adapter names and print info
        String[] adapters = driver.getAdapterNames();
        System.out.println("Number of adapters: " + adapters.length);
        //for (int i=0; i< adapters.length; i++) System.out.println("Device name in Java =" + adapters[i]);
        //Open first found adapter (usually first Ethernet card found)
        if (driver.openAdapter(adapters[0])) System.out.println("Adapter is open: " + adapters[0]);
        System.out.println();
        // 2 = vEthernet
        options = new Options();
        assembler = new IPAssembler();
        packet_sniffer = new PacketSniffer();
        rule_checker = new RuleChecker("rules.txt");
        new CommandLine(options).execute(args);

        if (options.output_filename != null) {
            writer = new PacketWriter(options.output_filename);
        }
    }

    public static Driver get() {
        return instance;
    }

    public void run() {
        // Send test packets on Ethernet if enabled
        if (options.input_filename != null) {
            sendTestPackets();
            System.out.println();
        }

        ArrayList<Signature> signatures = new ArrayList<>();
        Signature temp_signature;
        Packet packet;
        byte[] bytes;

        // Sniff and Parse packets until we should stop
        while (packet_sniffer.stillSniffing()) {
            packet_sniffer.sniff();
            packet = packet_sniffer.packet;
            bytes = packet_sniffer.bytes;

            // If the packet isn't filtered by our options
            if (!packet.isFiltered()) {
                // Write the raw bytes to a file if enabled
                if (writer != null) {
                    writer.write(bytes);
                }

                //
                rule_checker.check(packet);
                assembler.addFragment(packet);
                signatures = assembler.update();


                for (Signature signature : signatures) {
                    rule_checker.check(signature);
                    System.out.println(signature.toString());
                }
            }

            // Remove any timed-out Datagrams
            signatures.addAll(assembler.removeOld());

            // This is Project 2, so no need to do anything with these signatures.
            signatures.clear();
        }
        writer.close();
    }

    public void sendTestPackets() {
        FileSender file_sender = new FileSender();

        System.out.println("Reading Packets from " + options.input_filename + " and sending them as test packets");
        file_sender.sendFile(options.input_filename);
    }

    public static void main(String[] args) {
        Driver driver = Driver.get();
        driver.init(args);
        driver.run();
    }
}
