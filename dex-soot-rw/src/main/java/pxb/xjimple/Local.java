package pxb.xjimple;

import org.objectweb.asm.Type;

public class Local extends Value {
    public Type type;
    public String name;

    public int _ls_index;
    public int _ls_read_count;
    public int _ls_write_count;
    public ValueBox _ls_vb;

    public Local(String name, Type type) {
        super(Value.VT.LOCAL);
        this.type = type;
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
