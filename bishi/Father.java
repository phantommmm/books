import java.util.ArrayList;
import java.util.List;

public class Father {
    private static  volatile  Father father;
    public final int b=3;
    private Father(int a){}

    protected Father(){

    }

    public static Father getFather(){
    List<List<Integer>> list=new ArrayList<>();

        if (father==null){
            synchronized (Father.class){
                if (father==null){
                    father=new Father();
                }
            }
        }
        return father;
    }
}
