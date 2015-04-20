package j6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Thro {

    static Map<Throwable, List<Throwable>> aa = new HashMap<>();

    public static void addSuppressed(Throwable a, Throwable b) {
        List<Throwable> list = aa.get(a);
        if (list == null) {
            list = new ArrayList<>();
            aa.put(a, list);
        }
        list.add(b);
    }

    public static Throwable[] getSuppressed(Throwable a) {
        List<Throwable> list = aa.remove(a);
        if (list == null) {
            return null;
        }
        return list.toArray(new Throwable[list.size()]);
    }
}
