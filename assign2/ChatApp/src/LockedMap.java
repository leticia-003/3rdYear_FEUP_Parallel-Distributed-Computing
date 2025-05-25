import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe wrapper for a map using ReentrantLock for manual synchronization to avoid race conditions.
 *
 * This class wraps a regular HashMap and protects access using a ReentrantLock.
 * The keySet() and values() methods return copies to avoid issues when reading while others write.
 */
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


    public Iterable<K> keySet() {
        lock.lock();
        try {
            return new HashMap<>(map).keySet();
        } finally {
            lock.unlock();
        }
    }

    public Iterable<V> values() {
        lock.lock();
        try {
            return new HashMap<>(map).values();
        } finally {
            lock.unlock();
        }
    }
}
