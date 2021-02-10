import java.util.concurrent.locks.ReentrantLock;

/**
 * 实现一个多线程类，并用该线程类实例化 3 个线程 A,B,C ；
 * A 线程打印字符 A,B 线程打印字符 B，C 线程打印字符 C ；
 * 启动这 3 个线程，要求启动线程的顺序为 C 线程->B 线程->A 线程，并且最后输出内容为：A B C 。禁止使用 sleep 函数。
 */
class MyThread implements Runnable {

    private String name;

    public MyThread(String str) {
        this.name = str;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (TestThread.lock) {
                if (TestThread.str.equals("ABC")) {
                    System.out.println(TestThread.str);
                    break;
                }
                if (this.name.equals("A") && TestThread.str.equals("")) {
                    TestThread.str += "A";
                    TestThread.lock.notifyAll();
                } else if (this.name.equals("B") && TestThread.str.equals("A")) {
                    TestThread.str += "B";
                    TestThread.lock.notifyAll();
                } else if (this.name.equals("C") && TestThread.str.equals("AB")) {
                    TestThread.str += "C";
                    TestThread.lock.notifyAll();
                } else {
                    try {
                        TestThread.lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

class MyThread2 implements Runnable {

    private String name;

    public MyThread2(String str) {
        this.name = str;
    }

    @Override
    public void run() {
        while (true) {
            TestThread.lock2.lock();
            if (TestThread.str.equals("ABC")) {
                System.out.println(TestThread.str);
                TestThread.lock2.unlock();
                break;
            }
            if (this.name.equals("A") && TestThread.str.equals("")) {
                TestThread.str += "A";
            } else if (this.name.equals("B") && TestThread.str.equals("A")) {
                TestThread.str += "B";
            } else if (this.name.equals("C") && TestThread.str.equals("AB")) {
                TestThread.str += "C";
            }
            TestThread.lock2.unlock();
        }
    }
}

class MyThread3 implements Runnable {

    private String name;

    public MyThread3(String str) {
        this.name = str;
    }

    @Override
    public void run() {
        try {
            if (this.name.equals("C")) {
                TestThread.B.join();
            } else if (this.name.equals("B")) {
                TestThread.A.join();
            }
            System.out.print(name);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


public class TestThread {
    public static String str = "";
    public static Object lock = new Object();
    public static ReentrantLock lock2 = new ReentrantLock();
    public static Thread A;
    public static Thread B;
    public static Thread C;

    public static void main(String[] args) throws InterruptedException {
        A = new Thread(new MyThread3("A"));
        A.setName("A");
        B = new Thread(new MyThread3("B"));
        B.setName("B");
        C = new Thread(new MyThread3("C"));
        C.setName("C");
        C.start();
//        C.join();
        B.start();
//        B.join();
        A.start();
//        A.join();
    }
}
