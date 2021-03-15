import java.util.Arrays;

/**
 * 给定一个按照升序排列的整数数组 nums，和一个目标值 target。找出给定目标值在数组中的开始位置和结束位置。
 *
 * 如果数组中不存在目标值 target，返回 [-1, -1]。
 * 输入：nums = [5,7,7,8,8,10], target = 8
 * 输出：[3,4]
 *  nums=[1] target=1
 *  [0,0]
 *  [3,3,3] 3
 *  [0,2]
 */
public class searchRange {

    public static void main(String[] args) {
//        int[] nums={5,7,7,8,8,10};
//        int target=8;
//        int[] nums={1};
//        int target=1;
        int[] nums={3,3,3};
        int target=3;
        System.out.println(Arrays.toString(search(nums,target)));
    }


    public static int[] search(int[] nums,int target){
        int[] res=new int[2];
        int left=0,right=nums.length-1;

        while (left<=right){
            int mid=(left+right)/2;

            if (target>nums[mid]){
                left=mid+1;
            }else if (target<nums[mid]){
                right=mid-1;
            }else{
                int maxIdx=mid;
                int minIdx=mid;
                while (maxIdx+1<nums.length&&nums[maxIdx+1]==nums[mid]){
                    maxIdx++;
                }
                while (minIdx-1>=0&&nums[minIdx-1]==nums[mid]){
                    minIdx--;
                }
                res[0]=minIdx;
                res[1]=maxIdx;

                return res;
            }
        }
        res[0]=-1;
        res[1]=-1;

        return res;
    }
}
