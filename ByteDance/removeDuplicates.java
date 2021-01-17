/**
 *删除排序数组中的重复项
 * 给定数组 nums = [1,1,2],
 *
 * 函数应该返回新的长度 2, 并且原数组 nums 的前两个元素被修改为 1, 2。
 */
public class removeDuplicates {

    public static void main(String[] args) {
        removeDuplicates removeDuplicates=new removeDuplicates();
        int[] arr={1,1,2};
        System.out.println(removeDuplicates.fun(arr));
    }

    //双指针
    public int fun(int[] arr){
        if (arr.length==0){
            return 0;
        }
        int i=0;
        for (int j=1;j<arr.length;j++){
            if (arr[i]!=arr[j]){
                i++;
                arr[i]=arr[j];
            }
        }
        return i+1;//i是索引 长度+1
    }

}
