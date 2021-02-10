import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class sortArray {

    public static void main(String[] args) {
        int[] arr = {5, 1, 3, 9, 7, 5, 6};
        sortArray s = new sortArray();
        s.merge(arr);
        System.out.println(Arrays.toString(arr));
    }

    int[] temp;

    public void merge(int[] arr){
        temp=new int[arr.length];
        mergeSort(arr,0,arr.length-1);
    }

    public void mergeSort(int[] arr, int left, int right) {
        if (left>=right){
            return ;
        }

        int mid = (left + right) >> 1;
        mergeSort(arr,left,mid);
        mergeSort(arr,mid+1,right);
        //[i,mid] [j,right]
        int i=left,j=mid+1;
        int cnt=0;
        //放置到临时数组中
        while (i<=mid&&j<=right){
            if (arr[i]<arr[j]){
                temp[cnt++]=arr[i++];
            }else{
                temp[cnt++]=arr[j++];
            }
        }

        while (i<=mid){
            temp[cnt++]=arr[i++];
        }
        while (j<=right){
            temp[cnt++]=arr[j++];
        }
        //将排序好的部分放回原位
        for (int k=0;k<right-left+1;k++){
            arr[k+left]=temp[k];
        }

    }

    //非递归
    public void quickSort2(int[] arr){
        Stack<Integer> stack=new Stack<>();
        int left=0,right=arr.length-1;

        if (left<right){
            stack.push(right);
            stack.push(left);

            while (!stack.isEmpty()){
                int i=stack.pop();
                int j=stack.pop();

                int index=randomPartition(arr,i,j);

                if (index-1>i){
                    stack.push(index-1);
                    stack.push(i);
                }
                if (index+1<j){
                    stack.push(j);
                    stack.push(index+1);
                }
            }

        }
    }


    public void quickSort(int[] arr) {
        randomQuickSort(arr, 0, arr.length - 1);
    }

    private void randomQuickSort(int[] arr, int left, int right) {
        if (left < right) {
            int pos = randomPartition(arr, left, right);
            randomQuickSort(arr, left, pos - 1);
            randomQuickSort(arr, pos + 1, right);
        }
    }

    //随机获取中间点 中间点左边都小于它 右边都大于它
    private int randomPartition(int[] arr, int left, int right) {
        int idx = new Random().nextInt(right - left + 1) + left;
        swap(arr, idx, right);
        return partition(arr, left, right);
    }

    private int partition(int[] arr, int left, int right) {
        int pivot = arr[right];
        int i = left;

        //把小于中间点的移到左边
        for (int j = left; j < right; j++) {
            if (arr[j] <= pivot) {
                swap(arr, i++, j);
            }
        }
        //将原中间点与现在新中间点交换，即 i的左边都小于它 右边都大于它
        swap(arr, i, right);

        return i;
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
