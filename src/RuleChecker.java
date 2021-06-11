import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class RuleChecker {

    ArrayList<Rule> rules;

    public RuleChecker(String filename) {
        rules = new ArrayList<Rule>();
        try {
            readRules(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void readRules(String filename) throws FileNotFoundException {
        // Create scanner to read each line of the rules file
        Scanner line_scanner = new Scanner( new File(filename));

        // Read the rule on each line until there are no more lines
        while (line_scanner.hasNextLine()) {
            readRule(line_scanner.nextLine());
        }
    }

    public void readRule(String line) {
        Scanner word_scanner = new Scanner(line);
        Rule rule = new Rule();

        rule.action = word_scanner.next();
        readProtocol(rule, word_scanner);
        readIPMask(rule.ip1, word_scanner);
        readPort(rule.port_range1, word_scanner);
        readFlow(rule, word_scanner);
        readIPMask(rule.ip2, word_scanner);
        readPort(rule.port_range2, word_scanner);
        readOptions(rule, word_scanner);
        rules.add(rule);
    }

    public void readProtocol(Rule rule, Scanner word_scanner) {
        String protocol_str = word_scanner.next();
        rule.protocol = ProtocolType.getFromString(protocol_str);
    }

    public void readIPMask(IPAddress address, Scanner word_scanner) {
        String ip_mask = word_scanner.next();

        if (ip_mask.equals("any")) {
            address.any = true;
        } else {
            String[] thing = ip_mask.split("/");
            //address.bytes = Integer.parseUnsignedInt(thing[0]);
            String[] address_str = thing[0].split("\\.");
            for (int i = 0; i < address_str.length; i++) {
                int test = Integer.parseUnsignedInt(address_str[i]);
                int left_shift = ((address_str.length - i - 1) * 8);
                int test_2 = (test << left_shift);
                address.bytes = address.bytes | test_2;
            }
            address.setMask(Integer.parseUnsignedInt(thing[1]));
        }
    }

    public void readPort(PortRange port_range, Scanner word_scanner) {
        String port_str = word_scanner.next();
        String[] thing = port_str.split(":");
        if (thing.length == 1) {
            if (!thing[0].equals("any")) {
                port_range.port1 = Integer.parseUnsignedInt(thing[0]);
            }
            // Do nothing if "any"
        } else if (thing[0].equals("")) {
            port_range.port1 = -1;
            port_range.port2 = Integer.parseUnsignedInt(thing[1]);
        } else {
            port_range.port1 = Integer.parseUnsignedInt(thing[0]);
            port_range.port2 = Integer.parseUnsignedInt(thing[1]);
        }
    }

    public void readFlow(Rule rule, Scanner word_scanner)  {
        String flow = word_scanner.next();

        if(flow.equals("<>")) {
            rule.bi_dir = true;
        } else {
            rule.bi_dir = false;
        }
    }

    public void readOptions(Rule rule, Scanner word_scanner) {
        if (!word_scanner.hasNextLine()) {
            return;
        }
        // Get the rest of the line
        String options_str = word_scanner.nextLine();
        // Remove " (" and ")"
        options_str = options_str.substring(2, options_str.length() - 1);

        while(!options_str.isEmpty()) {
            // Get the index of the first ':'
            int index = options_str.indexOf(':');
            int end_index = options_str.indexOf(';');
            // The name of the command is everything before the ':'
            String command = options_str.substring(0, index);
            command = command.trim();
            String details = options_str.substring(index + 1, end_index);
            details = details.trim();


            switch (command) {
                case "msg":
                    // Remove leading and trailing "
                    details = details.substring(1, details.length() - 1);
                    rule.msg = details;
                    break;
                case "logto":
                    // Remove leading and trailing "
                    details = details.substring(1, details.length() - 1);
                    rule.log_filename = details;
                    break;
                case "ttl":
                    rule.options.add(new RuleTTL(details));
                    break;
                case "tos":
                    rule.options.add(new RuleTOS(details));
                    break;
                case "id":
                    rule.options.add(new RuleID(details));
                    break;
                case "fragoffset":
                    rule.options.add(new RuleFragOffset(details));
                    break;
                case "ipoption":
                    System.err.println("Option parsing not implemented");
                    break;
                case "fragbits":
                    rule.options.add(new RuleFragBits(details));
                    break;
                case "dsize":
                    rule.options.add(new RuleDSize(details));
                    break;
                case "flags":
                    rule.options.add(new RuleFlags(details));
                    break;
                case "seq":
                    rule.options.add(new RuleSEQ(details));
                    break;
                case "ack":
                    rule.options.add(new RuleACK(details));
                    break;
                case "itype":
                    rule.options.add(new RuleIType(details));
                    break;
                case "icode":
                    rule.options.add(new RuleICode(details));
                    break;
                case "content":
                    rule.options.add(new RuleContent(details));
                    break;
                case "sameip":
                    rule.options.add(new RuleSameIP());
                    break;
                case "sid":
                    rule.sid = Integer.parseUnsignedInt(details);
                    break;
                default:
                    System.err.println("RuleChecker.readOptions(): \"" + command + "\" did not match any options");
            }

            // Advance to next command
            options_str = options_str.substring(end_index + 1);
        }
    }

    /**
     * Since the packet of a Signature only contains everything past the IP
     * @param signature
     */
    public void check(Signature signature) {
        // If it's not an individual packet
        Packet packet;
        if (!signature.packet.equals(signature.fragments.get(0))) {
            packet = Packet.combinePackets(signature.fragments.get(0), signature.packet);
        } else {
            return;
        }
        check(packet);
    }

    public void check(Packet packet) {
        for (Rule rule : rules) {
            if(rule.matches(packet)) {
                break;
            }
        }
    }
}
