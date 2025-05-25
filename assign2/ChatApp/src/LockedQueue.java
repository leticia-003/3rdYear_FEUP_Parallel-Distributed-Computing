import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe queue using locks and condition variables.
 * This class allows safe access to a queue by multiple threads.
 */
public class LockedQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    public void add(T item) {
        lock.lock();
        try {
            queue.add(item);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            return queue.remove();
        } finally {
            lock.unlock();
        }
    }

    public T poll() {
        lock.lock();
        try {
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    public List<T> snapshot() {
        lock.lock();
        try {
            return new ArrayList<>(queue); // make a copy under lock
        } finally {
            lock.unlock();
        }
    }
}
