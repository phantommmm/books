/**
 * 扑克牌顺子
 * 大小王几张都一样
 */
public class isStraight {

    public static void main(String[] args) {
        isStraight i=new isStraight();
        int[] arr={1,2,3,4,5};
        System.out.println(i.fun(arr));
    }

    public boolean fun(int[] arr){
        int[] bucket=new int[14];

        for (int num:arr){
            if (num!=0&&bucket[num]==1){
                return false;
            }else{
                bucket[num]+=1;
            }
        }

        int max=-1,min=-1;

        for (int i=1,j=13;min==-1||max==-1;i++,j--){
            if (min==-1&&bucket[i]==1){
                min=i;
            }
            if (max==-1&&bucket[j]==1){
                max=j;
            }
        }

        return max-min<=4;
    }
}
