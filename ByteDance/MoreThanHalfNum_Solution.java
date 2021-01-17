/**
 * 数组中超过一半的数
 * {1,2,3,2,2,2,5,4,2}。由于数字2在数组中出现了5次，超过数组长度的一半，因此输出2。
 * 如果不存在则输出0。
 */
public class MoreThanHalfNum_Solution {
    public static void main(String[] args) {
        MoreThanHalfNum_Solution m=new MoreThanHalfNum_Solution();
        int[] arr={1,2,3,2,2,2,5,4,2};
        System.out.println(m.fun(arr));
    }

    public int fun(int [] array) {
        int res=array[0];
        int size=0;
        int n=array.length;
        for(int num:array){
            if(res==num){
                size++;
            }else if(size==0){
                res=num;
            }else{
                size--;
            }
        }
        //判断是否是众数
        size=0;
        for (int num:array){
            if (num==res){
                size++;
            }
        }
        return size>n/2?res:0;
    }
}

