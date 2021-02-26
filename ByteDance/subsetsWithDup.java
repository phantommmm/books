import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 重复元素的子集
 * 输入: [1,2,2]
 * 输出:
 * [
 *   [2],
 *   [1],
 *   [1,2,2],
 *   [2,2],
 *   [1,2],
 *   []
 * ]
 */
public class subsetsWithDup {

    List<List<Integer>> res;
    public List<List<Integer>> fun(int[] nums){
        Arrays.sort(nums);
        res=new ArrayList<>();
        backtrack(nums,0,new ArrayList<>());

        return res;
    }


    private void backtrack(int[] nums,int start,List<Integer> list){
        res.add(new ArrayList<>(list));

        for (int i=start;i<nums.length;i++){
            if (i>start&&nums[i]==nums[i-1]){
                continue;
            }
            list.add(nums[i]);
            backtrack(nums,i+1,list);
            list.remove(list.size()-1);
        }
    }
}
