package org.apache.commons.collections.map;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Transformer;

public class LazyMap<K, V> extends AbstractMap<K, V> {
    private Map<K, V> map;
    private Transformer factory;

    public LazyMap(Map<K, V> map, Transformer factor) {
        super();
        this.map = map;
        this.factory = factor;
    }

    public static <K, V> Map<K, V> decorate(Map<K, V> map, Transformer factory) {
        return new LazyMap<K, V>(map, factory);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        // create value for key if key is not currently in the map
        if (map.containsKey(key) == false) {
            V value = (V) factory.transform(key);
            map.put((K) key, value);
            return value;
        }
        return map.get(key);
    }

}
