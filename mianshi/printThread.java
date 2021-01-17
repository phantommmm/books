public class printThread implements Runnable{
    private int no;
    private static int count;
    private static Object lock=new Object();
    private static int flag=1;

    public  printThread(int no){
        this.no=no;
    }

    @Override
    public void run() {
        while (true){
            synchronized (lock){
                if (count>100){
                    break;
                }
                flag=flag>4?1:flag;

                if (flag==this.no){
                    System.out.println(no+"--"+count+"--"+Thread.currentThread().getId());
                    count++;
                    flag++;
                    lock.notifyAll();
                }else{
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        Thread thread1=new Thread(new printThread(1));
        Thread thread2=new Thread(new printThread(2));
        Thread thread3=new Thread(new printThread(3));
        Thread thread4=new Thread(new printThread(4));
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
    }
}
