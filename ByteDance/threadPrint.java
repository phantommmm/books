/**
 * 三个线程循环打印ABC
 */
public class threadPrint implements Runnable{
    private int no;
    private static int flag=0;
    private static Object lock=new Object();
    private static int count=1;
    private static char[] arr={'A','B','C'};

    public threadPrint(int no){
        this.no=no;
    }

    @Override
    public void run() {
        while (true){
            synchronized (lock){
                if (count>30){
                    break;
                }
                if (flag%3==this.no){
                    System.out.println(this.no+"--"+arr[no]+"--"+count);
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
        Thread t1=new Thread(new threadPrint(0));
        Thread t2=new Thread(new threadPrint(1));
        Thread t3=new Thread(new threadPrint(2));
        t1.start();
        t2.start();
        t3.start();
    }
}
