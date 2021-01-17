public class Thread1 implements Runnable {
    private int no;
    private static int count;
    private static Object lock=new Object();


    public Thread1(int no){
        this.no=no;
    }

    @Override
    public void run() {
        while (true){
            synchronized (lock){
                if (count>100){
                    break;
                }
                if (count%3==this.no){
                    System.out.println(no+"---"+count);
                    count++;
                }else{
                    try {
                        lock.wait();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                lock.notifyAll();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new Thread1(0));
        Thread t2 = new Thread(new Thread1(1));
        Thread t3 = new Thread(new Thread1(2));
        t1.start();
        t2.start();
        t3.start();
    }
}
