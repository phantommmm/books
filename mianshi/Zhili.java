import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Zhili {
    /**
     * 不用第三个变量交换两个变量的值
     */
    public static void main(String[] args) {
        fun();
    }

    public static void fun(){
        //当数很大时，可能超出范围
        int a=3,b=4,c=5,d=6;
        a=a+b;
        b=a-b;
        a=a-b;
        System.out.println(a+"=="+b);

        c=c^d;
        d=c^d;
        c=c^d;
        System.out.println(c+"=="+d);
    }
}
