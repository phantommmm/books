import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 三数之和 a+b+c=0
 */

/**
 * 给定数组 nums = [-1, 0, 1, 2, -1, -4]，
 * 满足要求的三元组集合为：
 * [
 * [-1, 0, 1],
 * [-1, -1, 2]
 * ]
 */
public class threeSum {

    public static void main(String[] args) {
        int[] nums={-1, 0, 1, 2, -1, -4};
        threeSum threeSum=new threeSum();
        System.out.println(threeSum.fun(nums).toString());
    }

    public List<List<Integer>> fun(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        //首先排序
        Arrays.sort(nums);//O(nlogn)

        //三个数所以length-2
        for (int i=0;i<nums.length-2;i++){
            //第一个数大于0 直接break;
            if (nums[i]>0) break;
            //相等直接跳过
            if (i>0&&nums[i]==nums[i-1]) continue;
            //target 为两数之和目标
            int target=-nums[i];
            //两数之和范围
            int left=i+1,right=nums.length-1;

            while (left<right){
                if (nums[left]+nums[right]==target){
                    res.add(new ArrayList<>(Arrays.asList(nums[i],nums[left],nums[right])));
                    //首先各走一步
                    left++;
                    right--;
                    //跳过重复
                    while (left<right&&nums[left]==nums[left-1]) left++;
                    while (left<right&&nums[right]==nums[right+1]) right--;
                }else if (nums[left]+nums[right]<target){
                    left++;
                }else{
                    right--;
                }
            }
        }
        return res;
    }
}