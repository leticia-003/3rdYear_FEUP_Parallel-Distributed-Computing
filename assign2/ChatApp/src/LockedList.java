import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
            return new ArrayList<>(list); // return a safe copy
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(T item) {
        lock.lock();
        try {
            return list.contains(item);
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
