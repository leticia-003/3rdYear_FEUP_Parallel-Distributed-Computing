import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LockedMap<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void put(K key, V value) {
        lock.lock();
        try {
            map.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        lock.lock();
        try {
            return map.get(key);
        } finally {
            lock.unlock();
        }
    }

    public boolean containsKey(K key) {
        lock.lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    public Iterable<K> keySet() {
        lock.lock();
        try {
            return new HashMap<>(map).keySet(); // safe copy
        } finally {
            lock.unlock();
        }
    }

    public Iterable<V> values() {
        lock.lock();
        try {
            return new HashMap<>(map).values(); // safe copy
        } finally {
            lock.unlock();
        }
    }
}
