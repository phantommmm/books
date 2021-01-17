import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Wei {

    public static void main(String[] args) {
        //aaabbccc输出a_3_b_2_c_3

        String str="aaabbccc";
        Map<Integer,Integer> map=new HashMap<>();

        System.out.println(map.get(1));
    }


    public static void main2(String[] args) {
        int n,m=0;
        Scanner sc=new Scanner(System.in);
        n=sc.nextInt();
        m=sc.nextInt();
        int[] arr=new int[n];
        for (int i=0;i<n;i++){
            arr[i]=sc.nextInt();
        }
        Arrays.sort(arr);
        int[] nums=new int[m];

        for (int i=0;i<m;i++){
            nums[i]=sc.nextInt();
        }
        for (int i=0;i<m;i++){
            int target=nums[i];
            int left=target-1;
            int right=target+1;
            if (target<arr[0]){
                System.out.println(arr[0]);
                continue;
            }
            if (target>arr[arr.length-1]){
                System.out.println(arr[arr.length-1]);
                continue;
            }
            while (true){
                if (search(arr, target)!=-1){
                    System.out.println(arr[search(arr,target)]);
                    break;
                }
//                if (search(arr, left)!=-1){
//                    System.out.println(arr[search(arr,left)]);
//                    break;
//                }
//                if (search(arr, right)!=-1){
//                    System.out.println(arr[search(arr,right)]);
//                    break;
//                }
//                left--;
//                right++;
            }
        }
    }

    public static int search(int[] nums,int target){
        int pivot,left=0,right=nums.length-1;
        int res=0;
        while (left<=right){
            pivot=left+(right-left)/2;
            if (nums[pivot]==target) return pivot;
            if (target<nums[pivot]) {
                right=pivot-1;
                res=right;
            }
            else {
                left=pivot+1;
                res=left;
            }
        }
        return res;
    }
}
