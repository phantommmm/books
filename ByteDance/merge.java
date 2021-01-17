/**
 * 合并两个有序数组
 * 给你两个有序整数数组 nums1 和 nums2，请你将 nums2 合并到 nums1 中，使 nums1 成为一个有序数组。
 */

import java.util.Arrays;

/**
 * 输入:
 * nums1 = [1,2,3,0,0,0], m = 3
 * nums2 = [2,5,6],       n = 3
 * <p>
 * 输出: [1,2,2,3,5,6]
 */
public class merge {
    public static void main(String[] args) {
        int[] arr1 = {1, 2, 3, 0, 0, 0};
        int[] arr2 = {2, 5, 6};
        merge merge = new merge();
        merge.fun(arr1, 3, arr2, 3);

        System.out.println(Arrays.toString(arr1));
    }

    public void fun(int[] arr1, int m, int[] arr2, int n) {
        int p = m + n - 1, p1 = m - 1, p2 = n - 1;

        while (p1 >= 0 && p2 >= 0) {
            arr1[p--] = arr1[p1] > arr2[p2] ? arr1[p1--] : arr2[p2--];
        }
        //如果p1指针遍历完成了但p2指针还没遍历完，此时应该继续遍历p2指针并填入p指针，
        // 由于p2是有序的，就直接用复制数组代替了遍历写入。
        // 如果p2指针遍历完了但p1指针还没遍历完，此时应该继续遍历p1并填入p指针，
        // 但由于此时p1指针和p指针重合，所以可以省略遍历操作。
        //p2是索引 长度=索引+1
        System.arraycopy(arr2, 0, arr1, 0, p2 + 1);
    }
}
