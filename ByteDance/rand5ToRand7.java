import java.util.Random;

/**
 * rang5 实现 rang7
 */
public class rand5ToRand7 {
    public static void main(String[] args) {
        rand5ToRand7 rand5ToRand7 = new rand5ToRand7();
        for (int i = 0; i < 50; i++) {
            System.out.println(rand5ToRand7.rand7());
        }
    }


    public int rand7() {
        //[1,5]->[0,4]->[0,20]->[1,25] 在1到25中均匀分散
        //3*7=21 大于21的不要
        int num=(rand5()-1)*5+rand5();
        while (num>21){
            num=(rand5()-1)*5+rand5();
        }
        //处理结果
        return 1+num%7;
    }

    public int rand5() {
        Random random = new Random();
        //[0,n)-->[0,5)
        //+1=[1,5]
        return random.nextInt(5)+1;
    }
}
