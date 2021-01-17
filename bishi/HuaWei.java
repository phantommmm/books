public class HuaWei {


    public static int fun(int[] arr) {
        int len = arr.length;
        int res = 0;
        for (int i = 0; i < len; i++) {
            int left = i;
            int curHeight = arr[i];
            while (left > 0 && arr[left - 1] >= curHeight) {
                left--;
            }
            int right = i;
            while (right < len - 1 && arr[right + 1] >= curHeight) {
                right++;
            }
            int width = right - left + 1;
            res = Math.max(res, width * curHeight);
        }
        return res;
    }
}
