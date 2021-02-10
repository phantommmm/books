import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockQueue {
    private final Condition isFull;
    private final Condition isEmpty;
    private final ReentrantLock lock;
    private volatile int capacity;
    private volatile int size;
    private final List<Integer> container;

    public MyBlockQueue() {
        lock = new ReentrantLock();
        isEmpty = lock.newCondition();
        isFull = lock.newCondition();
        capacity = 10;
        size = 0;
        container = new ArrayList<>();
    }

    public void add(Integer val) {
        try {
            lock.lock();
            while (size >= capacity) {
                System.out.println("queue is full");
                try {
                    isFull.await();
                } catch (InterruptedException e) {
                    isFull.signal();
                    e.printStackTrace();
                }
            }
            size++;
            container.add(val);
            isEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public int take() {
        try {
            lock.lock();
            while (size < 0) {
                System.out.println("queue is empty");
                try {
                    isEmpty.await();
                } catch (InterruptedException e) {
                    isEmpty.signal();
                    e.printStackTrace();
                }
            }
            size--;
            int res = container.get(0);
            container.remove(res);
            isFull.signal();
            return res;
        } finally {
            lock.unlock();
        }
    }


}
