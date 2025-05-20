import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockedQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    // Add item to queue
    public void add(T item) {
        lock.lock();
        try {
            queue.add(item);
            notEmpty.signal(); // wake up one waiting thread
        } finally {
            lock.unlock();
        }
    }

    // Take item from queue, wait if empty
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // block until not empty
            }
            return queue.remove();
        } finally {
            lock.unlock();
        }
    }

    // Optional: non-blocking poll
    public T poll() {
        lock.lock();
        try {
            return queue.poll(); // returns null if empty
        } finally {
            lock.unlock();
        }
    }
}
