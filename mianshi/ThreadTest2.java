/**
 * 编写一个程序，启动三个线程，三个线程的ID分别是A，B，C；，每个线程将自己的ID值在屏幕上打印5遍，打印顺序是ABCABC…
 */
public class ThreadTest2 {
    public static final Object lock = new Object();
    public static String flag = "A";

    public static void main(String[] args) {
        Thread A = new Thread(new MyThread4());
        Thread B = new Thread(new MyThread4());
        Thread C = new Thread(new MyThread4());
        A.setName("A");
        B.setName("B");
        C.setName("C");
        A.start();
        B.start();
        C.start();
    }


}

class MyThread4 implements Runnable {
    private int times = 0;

    @Override
    public void run() {
        while (times < 5) {
            synchronized (ThreadTest2.lock) {
                if (Thread.currentThread().getName().equals(ThreadTest2.flag)) {
                    System.out.print(ThreadTest2.flag);
                    this.times++;
                    if (ThreadTest2.flag.equals("A")) {
                        ThreadTest2.flag = "B";
                    } else if (ThreadTest2.flag.equals("B")) {
                        ThreadTest2.flag = "C";
                    } else if (ThreadTest2.flag.equals("C")) {
                        ThreadTest2.flag = "A";
                    }
                }
            }
        }
    }
}
