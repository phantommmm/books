import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 输入：candidates = [2,3,6,7], target = 7,
 * 所求解集为：
 * [
 * [7],
 * [2,2,3]
 * ]
 */
public class combinationSum {
    List<List<Integer>> res;

    public List<List<Integer>> fun(int[] nums, int target) {
        res = new ArrayList<>();
        Arrays.sort(nums);
        backtrack(nums, 0, target, new ArrayList<>());

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
            list.add(nums[i]);
            backtrack(nums, i, target - nums[i], list);
            list.remove(list.size() - 1);
        }
    }
}
