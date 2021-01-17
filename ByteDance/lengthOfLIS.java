import java.util.Arrays;

/**
 * 最长上升子序列
 */
public class lengthOfLIS {

    public static void main(String[] args) {
        int[] arr = {10, 9, 2, 5, 3, 7, 101, 18};
        int[] ar = {0};
        int[] a={1,2,8,6,4};
        lengthOfLIS lengthOfLIS = new lengthOfLIS();
        System.out.println(Arrays.toString(lengthOfLIS.LIS(a)));
    }


    public int[] LIS(int[] arr) {
        int n = arr.length;
        //长度
        int[] count = new int[n];
        //子序列
        int[] end = new int[n];
        int index = 0;
        count[0] = 1;
        end[0] = arr[0];

        for (int i = 0; i < n; i++) {
            if (end[index] < arr[i]) {
                end[++index] = arr[i];
                count[i] = index;
            } else {
                // 当前元素小于end中的最后一个元素即最大的元素
                // 利用二分法寻找第一个大于arr[i]的元素
                // end[left] 替换为当前元素 dp[]
                int left = 0, right = index;
                while (left <= right) {
                    int mid = (left + right) >> 1;
                    if (end[mid] >= arr[i]) {
                        right = mid - 1;
                    } else {
                        left = mid + 1;
                    }
                }
                System.out.println(end[left]+"--"+arr[i]);
                end[left] = arr[i];
                count[i] = left;
            }
        }

        int[] res=new int[index+1];
        for (int i=n-1;i>=0;i--){
            if (count[i]==index){
                res[index--]=arr[i];
            }
        }

        return res;
    }

    public int fun(int[] arr) {
        if (arr.length == 0) return 0;
        //dp[i]表示前i个数字的最长子序列
        int[] dp = new int[arr.length];
        //最短子序列为1
        Arrays.fill(dp, 1);
        int res = 0;

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < i; j++) {
                if (arr[i] > arr[j]) {
                    //大于 则dp[j]+1 然后比较
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            res = Math.max(res, dp[i]);
        }
        return res;
    }

    /**
     * 二分法
     *
     * @param arr
     * @return
     */
    public int fun2(int[] arr) {
        //tails[k] 的值代表 长度为 k+1 子序列 的尾部元素值。
        int[] tails = new int[arr.length];
        int res = 0;

        for (int num : arr) {
            //i和j指的是tails[]的左右边界
            int i = 0, j = res;
            //二分查找
            while (i < j) {
                int mid = (i + j) / 2;
                //大于 往下一个
                if (num > tails[mid]) {
                    i = mid + 1;
                } else {
                    //其它不变
                    j = mid;
                }
            }
            //覆盖/插入值
            tails[i] = num;
            //子序列长度+1
            if (res == j) {
                res++;
            }
        }
        return res;
    }
}
