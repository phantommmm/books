/**
 * 接雨水
 * 输入：height = [0,1,0,2,1,0,1,3,2,1,2,1]
 * 输出：6
 * 解释：上面是由数组 [0,1,0,2,1,0,1,3,2,1,2,1] 表示的高度图，
 * 在这种情况下，可以接 6 个单位的雨水（蓝色部分表示雨水）。
 */
public class trap {
    /**
     * 数组必有一个 ”山顶“（若有多个高度相同山顶，任取一个即可）。
     *
     * 根据”木桶原理“，山顶左侧的元素的盛水量 ，由左侧最大值决定；山顶右侧元素的盛水量，由右侧最大值决定。
     *
     * 双指针法的两个指针最终会停在 “山顶” 处。
     *
     *              top
     *               __
     *             _/  \       __
     *      __    /     \     /  \
     * _   /  \__/       \___/    \     __
     *  \_/                        \___/
     */
    public static int fun(int[] arr){
        int left=0,right=arr.length-1;
        int left_max=0,right_max=0;
        int res=0;

        while (left<right){
            if (arr[left]<arr[right]){
                if (arr[left]>left_max){
                    left_max=arr[left];
                }else{
                    res+=left_max-arr[left];
                }
                left++;
            }else{
                if (arr[right]>right_max){
                    right_max=arr[right];
                }else{
                    res+=right_max-arr[right];
                }
                right--;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        int[] arr={0,1,0,2,1,0,1,3,2,1,2,1};
        System.out.println(fun(arr));
    }
}
