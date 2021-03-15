/**
 * 找出该数组中满足其和 ≥ target 的长度最小的 连续子数组 [numsl, numsl+1, ..., numsr-1, numsr] ，
 * 并返回其长度。如果不存在符合条件的子数组，返回 0 。
 *
 * 输入：target = 7, nums = [2,3,1,2,4,3]
 * 输出：2
 * 解释：子数组 [4,3] 是该条件下的长度最小的子数组。
 */
public class minSubArrayLen {
    public static void main(String[] args) {
        int[] nums={2,3,1,2,4,3};
        int target=7;
        System.out.println(minSubArr(nums,target));
    }

    //滑动窗口
    public static int minSubArr(int[] nums,int target){
        int left=0,right=0,sum=0;
        int res=Integer.MAX_VALUE;

        while (right<nums.length){
            //右边界右移
            sum+=nums[right];
            //若窗口和>=target 左边界右移 用while
            while (sum>=target){
                res=Math.min(res,right-left+1);
                sum-=nums[left];
                left++;
            }
            right++;
        }

        return res==Integer.MAX_VALUE?0:res;
    }
}
