import java.util.Arrays;

/**
 * 实现获取 下一个排列 的函数，算法需要将给定数字序列重新排列成字典序中下一个更大的排列。
 * 如果不存在下一个更大的排列，则将数字重新排列成最小的排列（即升序排列）。
 * 输入：nums = [1,2,3]
 * 输出：[1,3,2]
 *
 * 输入 nums = [3,2,1];
 * 输出 nums = [1,2,3];
 */
public class nextPermutation {


    public void fun(int[] nums){
        int len=nums.length;

        for (int i=len-1;i>0;i--){
            //从后往前遍历找到相邻的 当前大于前一个的元素
            if (nums[i]>nums[i-1]){
                //对当前及后面进行排序 为了后面可以找到第一个大于i-1的元素
                Arrays.sort(nums,i,len);
                //找到第一个大于i-1的元素 swap
                for (int j=i;j<len;j++){
                    if (nums[j]>nums[i-1]){
                        swap(nums,j,i-1);
                        return ;
                    }
                }
            }
        }

        //没有找到则表明是最大的排列了 直接sort
        Arrays.sort(nums);
    }

    private void swap(int[] nums,int i,int j){
        int temp=nums[i];
        nums[i]=nums[j];
        nums[j]=temp;
    }

}
