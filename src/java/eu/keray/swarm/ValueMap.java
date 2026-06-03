package eu.keray.swarm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ValueMap<K> {
    private final HashMap<K, Value> values;
    Iterator<Map.Entry<K, Value>> iter;

    public ValueMap(int capacity) {
        this.values = new HashMap<>(capacity, 0.9F);
    }

    public K key;
    public Value val;

    public interface ReduceObserver<K> {
        void removed(K key);
    }

    static class Value {
        int val;
    }

    public void iter() {
        this.iter = this.values.entrySet().iterator();
    }

    public boolean next() {
        if (!this.iter.hasNext())
            return false;
        Map.Entry<K, Value> next = this.iter.next();
        this.key = next.getKey();
        this.val = next.getValue();
        return true;
    }

    public void remove() {
        this.iter.remove();
    }

    public void reduce(ReduceObserver<K> obs) {
        Iterator<Map.Entry<K, Value>> iter = this.values.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<K, Value> next = iter.next();
            Value v = next.getValue();
            if (--v.val < 1) {
                if (obs != null)
                    obs.removed(next.getKey());
                iter.remove();
            }
        }
    }

    public int remove(K key, int def) {
        Value v = this.values.remove(key);
        if (v == null) {
            return def;
        }
        return v.val;
    }

    public void put(K key, int value) {
        Value v = this.values.get(key);
        if (v == null) {
            v = new Value();
            this.values.put(key, v);
        }
        v.val = value;
    }

    public int increment(K key, int increment) {
        Value v = this.values.get(key);
        if (v == null) {
            v = new Value();
            this.values.put(key, v);
        }
        int old = v.val;
        v.val += increment;
        return old;
    }

    public int incrementExisting(K key, int increment) {
        Value v = this.values.get(key);
        if (v == null) {
            return -1;
        }
        return v.val++;
    }

    public int size() {
        return this.values.size();
    }

    public void clear() {
        this.values.clear();
    }

    public Set<K> keys() {
        return this.values.keySet();
    }
}
