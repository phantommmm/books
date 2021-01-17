/**
 * 数组中出现次数超过一半的数字
 * 输入: [1, 2, 3, 2, 2, 2, 5, 4, 2]
 * 输出: 2
 */
public class majorityElement {
    public static void main(String[] args) {
        majorityElement majorityElement=new majorityElement();
        int[] arr={1, 2, 3, 2, 2, 2, 5, 4, 2};
        System.out.println(majorityElement.fun(arr));
    }


    public int fun(int[] arr){
        //x为票 votes为票数
        int x=0,votes=0;
        //摩尔投票法 相同+1 不同抵消-1
        for(int num:arr){
            if (votes==0){
                x=num;
            }else{
                votes+=x==num?1:-1;
            }
        }
        return x;
    }

}
