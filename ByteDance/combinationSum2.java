import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * candidates 中的每个数字在每个组合中只能使用一次。
 * 输入: candidates =[10,1,2,7,6,1,5], target =8,
 * 所求解集为:
 * [
 *   [1, 7],
 *   [1, 2, 5],
 *   [2, 6],
 *   [1, 1, 6]
 * ]

 */
public class combinationSum2 {
    int[][] dir;

    public static void main(String[] args) {
        int[][] dir={{0,1},{1,0},{0,-1},{-1,0}};

        System.out.println(5/10);
        String str="13";
    }

    List<List<Integer>> res;
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        res = new ArrayList<>();
        Arrays.sort(candidates);
        backtrack(candidates, 0, target, new ArrayList<>());
        return res;
    }

    private void backtrack(int[] nums, int start, int target, List<Integer> list) {
        if (target == 0) {
            res.add(new ArrayList<>(list));
            return;
        }

        for (int i = start; i < nums.length; i++) {
            if (target < nums[i]) {
                break;
            }
            if(i>start&&nums[i]==nums[i-1]){
                continue;
            }
            list.add(nums[i]);
            backtrack(nums, i+1, target - nums[i], list);
            list.remove(list.size() - 1);
        }
    }


}
