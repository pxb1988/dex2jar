package res;

public class I71 {

    /**
     * code similar to edu.emory.mathcs.backport.java.util.concurrent.ConcurrentSkipListMap.SubMap.size()
     * 
     * @return
     */
    public int size() {
        long count = 0;
        for (int i = 0; i < 5; i++) {
            ++count;
        }
        return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
    }

}
