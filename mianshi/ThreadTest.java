import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ThreadTest implements Runnable {

    private static Object lock = new Object();

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (lock) {
                    System.out.println("runnnnnn");
                    lock.wait();
                }
            }catch (InterruptedException e){
                System.out.println("-----");
               // return ;
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
//        Thread t = new Thread(new ThreadTest());
//        t.start();
//        Thread.sleep(2000);
//        t.interrupt();
        String str="a";
        String str2="a";
        System.out.println(str==str2);
        System.out.println(str.equals(str2));
//        try{
//            System.out.println("try");
//            int i=1/0;
//            System.out.println("3333");
//            return;
//        }catch (Exception e){
//            System.out.println("catch");
//            return ;
//        }finally {
//            System.out.println("finally");
//            return ;
//        }
    }
}
