/**找出重复值 数字都在 1-n之间
 * 输入：nums = [1,3,4,2,2]
 * 输出：2
 */
public class findDuplicate {

    public static void main(String[] args) {
        int[] nums={1,3,4,2,2};
        System.out.println(findDunplicate(nums));
    }


    public static int findDunplicate(int[] nums){
        //不是索引而是元素值 最大/最小
        int left=1,right=nums.length-1;

        while (left<right){
            int mid=(left+right)/2;
            int cnt=0;

            //记录小于mid的个数
            for(int num:nums){
                if(num<=mid){
                    cnt++;
                }
            }

            //如果cnt大于mid 说明重复的数在[left,mid]，否则在[mid+1,right]
            if (cnt>mid){
                right=mid;
            }else{
                left=mid+1;
            }
        }

        return left;
    }
}
