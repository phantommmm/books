//public class QuickSort {
//    public void quickSort(int[] nums, int low, int high) {
//        if (low < high) {
//            int index = this.getIndex(nums, low, high);
//            quickSort(nums, low, index - 1);
//            quickSort(nums, index + 1, high);
//        }
//    }
//
//    private int getIndex(int[] nums, int low, int high) {
//        int temp = nums[low];
//
//        while (low < high) {
//            while (low < high && temp <= nums[high]) {
//                high--;
//            }
//            //此时nums[high]小于temp 所以放到最左边
//            nums[low] = nums[high];
//
//            while (low<high&&temp>=nums[low]){
//                low++;
//            }
//            //此时nums[low]大于temp 所以和刚刚high位置交换
//            nums[high]=nums[low];
//        }
//        //恢复原值
//        nums[low]=temp;
//        return low;
//    }
//}
