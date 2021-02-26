import java.util.ArrayList;
import java.util.List;

public class permuteUnique {

    List<List<Integer>> res;
    boolean[] used;
    public List<List<Integer>> fun(int[] nums){
        res=new ArrayList<>();
        used=new boolean[nums.length];
        backtrack(nums,new ArrayList<>());

        return res;
    }

    private void backtrack(int[] nums,List<Integer> list){
        if (list.size()== nums.length){
            res.add(list);
            return ;
        }

        for (int i=0;i<nums.length;i++){
            if (used[i]){
                continue;
            }
            if (i>0&&nums[i]==nums[i-1]&&used[i-1]){
                break;
            }
            list.add(nums[i]);
            used[i]=true;
            backtrack(nums,list);
            list.remove(list.size()-1);
            used[i]=false;
        }
    }
}
