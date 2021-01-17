import java.util.Arrays;

/**
 * 给定一个数组 nums 和滑动窗口的大小 k，请找出所有滑动窗口里的最大值
 * 输入: nums = [1,3,-1,-3,5,3,6,7], 和 k = 3
 * 输出: [3,3,5,5,6,7]
 * 解释:
 * <p>
 * 滑动窗口的位置                最大值
 * ---------------               -----
 * [1  3  -1] -3  5  3  6  7       3
 * 1 [3  -1  -3] 5  3  6  7       3
 * 1  3 [-1  -3  5] 3  6  7       5
 * 1  3  -1 [-3  5  3] 6  7       5
 * 1  3  -1  -3 [5  3  6] 7       6
 * 1  3  -1  -3  5 [3  6  7]      7
 */
public class maxSlidingWindow {

    public static void main(String[] args) {
        int[] arr={1,3,-1,-3,5,3,6,7};

        System.out.println(Arrays.toString(fun(arr,3)));
    }

    /**
     * // 1、处理第一个窗口
     * // 2、处理之后的窗口：
     *
     * // 上一轮最大值，在当前窗口内，可以省些计算
     * // 上一轮最大值，不再当前窗口内
     * // 当前窗口的最后一个元素，比上一个窗口的最大值大，表示当前窗口的最后一个元素就是当前窗口的最大值
     * // 当前窗口的最后一个元素，比上一个窗口的最大值小，重新计算 当前窗口最大值
     */
    public static int[] fun(int[] arr,int k){
        if (arr==null||k<=0||arr.length<k){
            return new int[0];
        }

        int len=arr.length-k+1;
        int max=arr[0],index=0;
        int left=0,right=k-1;
        int[] res=new int[len];

        for (int i=1;i<k;i++){
            if (arr[i]>max){
                max=arr[i];
                index=i;
            }
        }

        res[0]=max;

        for (int i=1;i<len;i++){
            left++;
            right++;

            if(left<=index){
                if(arr[right]>max){
                    max=arr[right];
                    index=right;
                }
            }else{
                if(arr[right]>max){
                    max=arr[right];
                    index=right;
                }else{
                    max=arr[left];
                    index=left;

                    for (int j=left;j<=right;j++){
                        if (arr[j]>max){
                            max=arr[j];
                            index=j;
                        }
                    }
                }
            }
            res[i]=max;
        }

        return res;
    }
}
