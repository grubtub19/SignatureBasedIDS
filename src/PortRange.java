/**
 * If both ports are -1, then it is any
 */
public class PortRange {

    // -1 is unassigned;
    int port1 = -1;
    int port2 = -1;

    public boolean matches(int port) {
        // any
        if (port1 == -1 && port2 == -1) {
            return true;
        //  port1:port2
        } else if (port != -1 && port2 != -1) {
            return port >= port1 && port <= port2;
        // :port2
        } else if (port == -1 && port2 != -2) {
            return port <= port2;
        // port1
        } else {
            return port == port1;
        }
    }

}
