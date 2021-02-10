import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产者消费者 生产者容量到达阈值后堵塞，消费者没有产品可以消费时堵塞
 * 多个生产者同时生产 多个消费者同时消费
 * <p>
 * <p>
 * 1. wait notify 完成
 * 2. lock condition 完成
 */
public class ProducterAndConsumer {
    public static Object lock = new Object();
    public static int capacity = 10;
    public static int count = 0;
    public static ReentrantLock lock2 = new ReentrantLock();
    public static Condition full = lock2.newCondition();
    public static Condition empty = lock2.newCondition();

    public static void main(String[] args) {
        Thread pro1 = new Thread(new Producter());
        Thread pro2 = new Thread(new Producter());
        Thread pro3 = new Thread(new Producter());
        Thread con1 = new Thread(new Consumer());
        Thread con2 = new Thread(new Consumer());
        Thread con3 = new Thread(new Consumer());
        pro1.setName("pro1");
        pro2.setName("pro2");
        pro3.setName("pro3");
        con1.setName("con1");
        con2.setName("con2");
        con3.setName("con3");
        pro1.start();
        pro2.start();
        pro3.start();
        con1.start();
        con2.start();
        con3.start();

//        Thread pro1 = new Thread(new Producter2());
//        Thread pro2 = new Thread(new Producter2());
//        Thread pro3 = new Thread(new Producter2());
//        Thread con1 = new Thread(new Consumer2());
//        Thread con2 = new Thread(new Consumer2());
//        Thread con3 = new Thread(new Consumer2());
//        pro1.setName("pro1");
//        pro2.setName("pro2");
//        pro3.setName("pro3");
//        con1.setName("con1");
//        con2.setName("con2");
//        con3.setName("con3");
//        pro1.start();
//        pro2.start();
//        pro3.start();
//        con1.start();
//        con2.start();
//        con3.start();
    }

}


class Producter implements Runnable {
    @Override
    public void run() {
        while (true) {
            synchronized (ProducterAndConsumer.lock) {
                if (ProducterAndConsumer.count < ProducterAndConsumer.capacity) {
                    ProducterAndConsumer.count++;
                    System.out.println(Thread.currentThread().getName() + " produce " + ProducterAndConsumer.count);
                    ProducterAndConsumer.lock.notifyAll();//每次生产都唤醒其他线程包括 生产者 or 消费者
                }else{
                    try {
                        ProducterAndConsumer.lock.wait();//等待消费
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

class Consumer implements Runnable {
    @Override
    public void run() {
        while (true) {
            synchronized (ProducterAndConsumer.lock) {
                if (ProducterAndConsumer.count > 0) {
                    ProducterAndConsumer.count--;
                    System.out.println(Thread.currentThread().getName() + " consume " + ProducterAndConsumer.count);
                    ProducterAndConsumer.lock.notifyAll();//每次消费都唤醒其他线程包括 生产者 or 消费者 可能存在唤醒的还是消费者
                }else{
                    try {
                        ProducterAndConsumer.lock.wait();//等待生成
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

class Producter2 implements Runnable {
    @Override
    public void run() {
        while (true) {
            ProducterAndConsumer.lock2.lock();
            if (ProducterAndConsumer.count < ProducterAndConsumer.capacity) {
                ProducterAndConsumer.count++;
                System.out.println(Thread.currentThread().getName() + " produce " + ProducterAndConsumer.count);
                ProducterAndConsumer.empty.signalAll();//每次生产都唤醒消费者消费
            }else{
                //产品已满才堵塞 这样会一个生产者一直生产到满
                try {
                    ProducterAndConsumer.full.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //每生产一个产品就堵塞 这样多个生产者交叉地生产
//            try {
//                ProducterAndConsumer.full.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            ProducterAndConsumer.lock2.unlock();
        }

    }
}

class Consumer2 implements Runnable {
    @Override
    public void run() {
        while (true) {
            ProducterAndConsumer.lock2.lock();
            if (ProducterAndConsumer.count > 0) {
                ProducterAndConsumer.count--;
                System.out.println(Thread.currentThread().getName() + " consume " + ProducterAndConsumer.count);
                ProducterAndConsumer.full.signalAll();//每次消费都唤醒生产者生产
            }else{
                try {
                    //没有产品可以消费了
                    ProducterAndConsumer.empty.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ProducterAndConsumer.lock2.unlock();
        }
    }
}