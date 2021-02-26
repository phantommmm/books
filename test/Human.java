import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Human {

    public void say(Human human) {
        System.out.println("Human");
    }

    //    public void say(Woman human) {
//        System.out.println("Woman");
//    }
//
    public void say(Man man) {
        System.out.println("Man");
    }

    public void s(Integer a) {
    }


    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(0);
        Integer[] arr = new Integer[1];
        arr = list.toArray(new Integer[1]);
        System.out.println(arr[0]);
        list=Arrays.asList(arr);
    }
}

class Man extends Human {

//    @Override
//    public void say() {
//        System.out.println("mmmm");
//    }
//
//    public void say2() {
//        System.out.println("Man man");
//    }

    public void s(String a) {

    }
}

class Woman extends Human {

}