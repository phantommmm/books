/**
 * 最大子序列和
 */

/**
 * 输入: [-2,1,-3,4,-1,2,1,-5,4]
 * 输出: 6
 * 解释: 连续子数组 [4,-1,2,1] 的和最大，为 6。
 */
public class maxSubArray {

    public static void main(String[] args) {
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        maxSubArray maxSubArray = new maxSubArray();
        System.out.println(maxSubArray.fun(nums));
    }

    //f(i)=max(f(i-1)+ai,ai)
    public int fun(int[] nums) {
        int pre = 0, maxAns = nums[0];
        for (int num : nums) {
            pre = Math.max(num, pre + num);
            maxAns = Math.max(maxAns, pre);
        }
        return maxAns;
    }

}
