import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe list wrapper using ReentrantLock for manual synchronization.
 * 
 * This class provides basic list operations (add, remove, contains, size) and a 
 * safe way to obtain a consistent snapshot of the current list.
 */
public class LockedList<T> {
    private final List<T> list = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void add(T item) {
        lock.lock();
        try {
            list.add(item);
        } finally {
            lock.unlock();
        }
    }

    public void remove(T item) {
        lock.lock();
        try {
            list.remove(item);
        } finally {
            lock.unlock();
        }
    }

    public List<T> snapshot() {
        lock.lock();
        try {
            return new ArrayList<>(list);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }
}
