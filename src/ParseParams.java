import java.nio.ByteBuffer;

public class ParseParams {

    public byte b;
    public ByteBuffer buffer;
    // How far into the byte b we currently are
    public int original_start_index;
    public boolean finished;

    public ParseParams(byte b, ByteBuffer buffer) {
        this.b = b;
        this.buffer = buffer;
        this.original_start_index = 0;
        this.finished = false;
    }

    public ParseParams(byte b, ByteBuffer buffer, int original_start_index) {
        this.b = b;
        this.buffer = buffer;
        this.original_start_index = original_start_index;
        this.finished = false;
    }
}
