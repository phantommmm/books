import java.util.ArrayList;
import java.util.List;

/**
 * 子集
 * 输入：nums = [1,2,3]
 * 输出：[[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]
 */
public class subsets {
    List<List<Integer>> res;
    public List<List<Integer>> fun(int[] nums){
        res=new ArrayList<>();
        backtrack(nums,0,new ArrayList<>());

        return res;
    }

    private void backtrack(int[] nums,int start,List<Integer> list){
        res.add(new ArrayList<>(list));

        for (int i=start;i<nums.length;i++){
            list.add(nums[i]);
            backtrack(nums,i+1,list);
            list.remove(list.size()-1);
        }
    }

}
