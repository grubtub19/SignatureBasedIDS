public class IPAddress {

    boolean any;
    // unsigned int is 4 bytes long
    int bytes;

    int mask = -1;

    public boolean matches(int other_address) {
        if (any) {
            return true;
        } else {
            return (bytes & mask) == (other_address & mask);
        }
    }

    public void setMask(int mask) {
        this.mask = 0xffffffff << (32 - mask);
    }
}
