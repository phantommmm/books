/**
 * 寻找峰值
 * 输入：nums = [1,2,1,3,5,6,4]
 * 输出：1 或 5
 * 解释：你的函数可以返回索引 1，其峰值元素为 2；
 *      或者返回索引 5， 其峰值元素为 6。
 */
public class findPeakElement {
    public static void main(String[] args) {
        int[] nums={1,2,1,3,5,6,4};
        System.out.println(findPeakEle(nums));
    }

    public static int findPeakEle(int[] nums){
        int left=0,right=nums.length-1;

        while (left<right){
            int mid=(left+right)/2;

            //峰值在[mid+1,right]
            if (nums[mid]<nums[mid+1]){
                left=mid+1;
            }else{
                //峰值在[left,mid]
                right=mid;
            }
        }

        return nums[left];
    }
}
