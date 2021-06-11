import picocli.CommandLine;

public class Options implements Runnable {
    @CommandLine.Option(names = "-c", paramLabel = "count", description = "Exit after receiving count packets")
    public int count = -1;

    @CommandLine.Option(names = "-os", paramLabel = "operating_system", description = "The type of operating system to be" +
            "used for packet reassembly. Linux overwrites, Windows doesn't.")
    public String os = "windows";

    @CommandLine.Option(names = "-r", paramLabel = "input_file", description = "Read packets from file (your program " +
            "should read packets from the network by default)")
    public String input_filename;

    @CommandLine.Option(names = "-o", paramLabel = "output_file", description = "Save output to filename")
    public String output_filename;

    @CommandLine.Option(names = "-t", paramLabel = "type", description = "Print only packets of the specified type " +
            "where type is one of: ${COMPLETION-CANDIDATES}")
    public ProtocolType protocol;

    @CommandLine.Option(names = "-h", description = "Print header info only as specified by -t")
    public boolean print_header;

    @CommandLine.Option(names = "-dpf", description = "Dont' print ip fragments")
    public boolean dont_print_fragments;

    @CommandLine.Option(names = "-src", paramLabel = "source_address", description = "Print only packets with source " +
            "address equal to source_address")
    public String source_address;

    @CommandLine.Option(names = "-dst", paramLabel = "destination_address", description = "Print only packets with" +
            " destination address equal to destination_address")
    public String destination_address;

    @CommandLine.Option(names = "-sord", arity= "2", paramLabel = "source_address destination_address", description = "Print only " +
            "packets where the source address matches source_address or the destination address matches " +
            "destination_address")
    private String[] or_addresses;
    public String or_source_address, or_destination_address;

    @CommandLine.Option(names = "-sandd", arity= "2", paramLabel = "source_address destination_address", description = "Print only " +
            "packets where the source address matches source_address and the destination address matches " +
            "destination_address")
    private String[] and_addresses;
    public String and_source_address, and_destination_address;

    @CommandLine.Option(names = "-sport", arity= "1..2", paramLabel = "source_port_start source_port_end", description = "Print only " +
            "packets where the source port is in the range source_port_start - source_port_end")
    private int[] source_ports;
    public int source_port_start = -1, source_port_end = -1;

    @CommandLine.Option(names = "-dport", arity= "1..2", paramLabel = "destination_port_start destination_port_end", description =
            "Print only packets where the destination port is in the range destination_port_start - destination_port_end")
    private int[] destination_ports;
    public int destination_port_start = -1, destination_port_end = -1;

    @CommandLine.Option(names = "-help", usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;

    @Override
    public void run() {
        parseArgs();
    }



    public void parseArgs() {
        // Set source and destination address for "or"
        if (or_addresses != null && or_addresses.length == 2) {
            or_source_address = or_addresses[0];
            or_destination_address = or_addresses[1];
        }

        // Set source and destination address for "and"
        if (and_addresses != null && and_addresses.length == 2) {
            and_source_address = and_addresses[0];
            and_destination_address = and_addresses[1];
        }

        // Set source start and end ports
        // If only one port is input, it is assigned as both
        if (source_ports != null) {
            source_port_start = source_ports[0];
            if (source_ports.length == 1) {
                source_port_end = source_ports[0];
            } else {
                source_port_end = source_ports[1];
            }
        }

        // Set source start and end ports
        // If only one port is input, it is assigned as both
        if (destination_ports != null) {
            destination_port_start = destination_ports[0];
            if (destination_ports.length == 1) {
                destination_port_end = destination_ports[0];
            } else {
                destination_port_end = destination_ports[1];
            }
        }
    }

    public boolean shouldPrint(Protocol protocol) {
        return !(print_header && this.protocol != null && this.protocol.getAsClass() != protocol.getClass());
    }

    @Override
    public String toString() {
        return "Options{" +
                "count=" + count +
                ", input_filename='" + input_filename + '\'' +
                ", output_filename='" + output_filename + '\'' +
                ", protocol=" + protocol +
                ", print_header=" + print_header +
                ", source_address='" + source_address + '\'' +
                ", destination_address='" + destination_address + '\'' +
                ", or_source_address='" + or_source_address + '\'' +
                ", or_destination_address='" + or_destination_address + '\'' +
                ", and_source_address='" + and_source_address + '\'' +
                ", and_destination_address='" + and_destination_address + '\'' +
                ", source_port_start='" + source_port_start + '\'' +
                ", source_port_end='" + source_port_end + '\'' +
                ", destination_port_start='" + destination_port_start + '\'' +
                ", destination_port_end='" + destination_port_end + '\'' +
                ", helpRequested=" + helpRequested +
                '}';
    }
}
