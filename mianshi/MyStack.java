import java.util.LinkedList;
import java.util.Queue;

/**
 * 队列实现栈
 */
public class MyStack {
    //queue 左出右进
    Queue<Integer> queue;

    public MyStack(){
        queue=new LinkedList<>();
    }

    public void push(int x){
        queue.add(x);
        //把前面的值从左边出去到右边添加 因为size不变，所以不会出错
       for (int i=1;i<queue.size();i++){
            queue.add(queue.remove());
        }
    }

    public int pop(){
        return queue.poll();
    }

    public int top(){
        return queue.peek();
    }

    public boolean empty(){
        return queue.size()==0;
    }
}
