import java.util.Arrays;
import java.util.Date;

/**
 * 返回数组前k大值
 */
public class TopK {

    int[] res;
    int idx;

    public static void main(String[] args) throws InterruptedException {
//        TopK t = new TopK();
//        int[] nums = {3, 2, 1, 5, 6, 7, 7, 78, 999};
//        int k = 3;
//        System.out.println(Arrays.toString(t.topK(nums, nums.length - k, k)));
//        System.out.println(Arrays.toString(t.topK2(nums, k, k)));
    }


    /**
     * 前K大个数
     */
    public int[] topK(int[] nums, int k, int count) {
        res = new int[count];
        idx = 0;
        partition(nums, 0, nums.length - 1, k, count);
        return res;
    }

    /**
     * 前K小个数
     */
    public int[] topK2(int[] nums, int k, int count) {
        res = new int[count];
        idx = 0;
        partition2(nums, 0, nums.length - 1, k, count);
        return res;
    }


    private void partition2(int[] nums, int left, int right, int k, int count) {
        if (left > right) {
            return;
        }
        int i = left, pivot = nums[right];

        for (int j = left; j < right; j++) {
            if (nums[i] < pivot) {
                swap(nums, i++, j);
            }
        }

        swap(nums, i, right);

        //前K小
        if (k == i) {
            while (idx < k) {
                res[idx] = nums[idx];
                idx++;
            }
        } else if (i < k) {
            partition2(nums, i + 1, right, k, count);
        } else if (i > k) {
            partition2(nums, left, i - 1, k, count);
        }
    }

    private void partition(int[] nums, int left, int right, int k, int count) {
        if (left > right) {
            return;
        }
        int i = left, pivot = nums[right];

        for (int j = left; j < right; j++) {
            if (nums[i] < pivot) {
                swap(nums, i++, j);
            }
        }

        swap(nums, i, right);

        //前K大
        if (k == i) {
            while (count > 0) {
                res[idx++] = nums[i++];
                count--;
            }
        } else if (i < k) {
            partition(nums, i + 1, right, k, count);
        } else if (i > k) {
            partition(nums, left, i - 1, k, count);
        }

    }

    private void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

}
