import java.util.Arrays;

public class threeSumClosest {

    public static void main(String[] args) {
        int[] arr={-3,-2,-5,3,-4};
        int target=-1;
        System.out.println(threeSumClosest(arr,target));
    }

    public static int threeSumClosest(int[] nums, int target) {
        int res=nums[0]+nums[1]+nums[2];
        Arrays.sort(nums);

        for(int i=2;i<nums.length;i++){
            int left=0,right=i-1;

            while(left<right){
                int sum=nums[i]+nums[left]+nums[right];
                if(Math.abs(sum-target)<Math.abs(res-target)){
                    res=sum;
                }

                if(sum<target){
                    left++;
                }else if(sum>target){
                    right--;
                }else{
                    return target;
                }
            }
        }

        return res;
    }
}
