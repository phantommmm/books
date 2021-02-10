import java.util.ArrayList;
import java.util.List;

/**
 * 全排列
 */
public class permute {
    List<List<Integer>> res;


    public static void main(String[] args) {
        int[] nums={1,2,3};
        permute p=new permute();
        System.out.println(p.fun(nums));
    }

    public List<List<Integer>> fun(int[] nums){
        res=new ArrayList<>();
        backtrack(nums,0);

        return res;
    }

    private void backtrack(int[] nums,int start){
        if (start==nums.length-1){
            List<Integer> list=new ArrayList<>();

            for(int num:nums){
                list.add(num);
            }
            res.add(list);
            return ;
        }

        for(int i=start;i<nums.length;i++){
            swap(nums,start,i);
            backtrack(nums,start+1);
            //回溯回来 保持原来状态
            swap(nums,start,i);
        }
    }

    private void swap(int[] nums,int i,int j){
        int temp=nums[i];
        nums[i]=nums[j];
        nums[j]=temp;
    }

}
