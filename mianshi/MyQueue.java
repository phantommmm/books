import java.util.LinkedList;

/**
 * 栈实现队列
 */
public class MyQueue {
    //一个做入队 一个做出队
    LinkedList<Integer> A, B;

    public MyQueue() {
        A = new LinkedList<>();
        B = new LinkedList<>();
    }

    public void appendTail(int x) {
        A.addLast(x);
    }

    public int deletedHead() {
        if (!B.isEmpty()) {
            return B.removeLast();
        }
        if (A.isEmpty()) {
            return -1;
        }
        while (!A.isEmpty()){
            B.addLast(A.removeLast());
        }
        return B.removeLast();
    }
}
