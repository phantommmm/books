public class MyThread1 implements Runnable {
    private static Object lock = new Object();
    private static int count;
    private int no;
    public MyThread1(int no) {
        this.no = no;
    }
    @Override
    public void run() {
        while (true) {
            synchronized (lock) {
                if (count > 100) {
                    break;
                }
                if (count % 3 == this.no) {
                    System.out.println(this.no + "--->" + count);
                    count++;
                } else {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                lock.notifyAll();
            }
        }
    }


        public static void main(String[] args) throws InterruptedException {
            Thread t1 = new Thread(new MyThread1(0));
            Thread t2 = new Thread(new MyThread1(1));
            Thread t3 = new Thread(new MyThread1(2));
            t1.start();
            t2.start();
            t3.start();
        }

}