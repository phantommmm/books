import java.util.Arrays;
import java.util.Scanner;

public class Wei {

    public static void main(String[] args) {
//        int[] arr = {1, 2, 3, 3, 4, 4, 5, 5, 6, 2};
//        System.out.println(Arrays.toString(dup2(arr)));
        int[] arr2 = {1, 2, 3, 4, 5, 5, 6, 7, 8, 9, 10, 10, 11};
        //第一个数A,第二个数B，其他异或和为C
        //A^A^B^B^C 1
        //A^B^C 2
        //A^B 1^2
        System.out.println(Arrays.toString(dup(arr2)));
        System.out.println(Arrays.toString(fun5(arr2)));
    }

    public static int[] dup(int[] arr) {
        int[] res = new int[2];
        int temp = arr[0];

        for (int i = 1; i < arr.length; i++) {
            temp ^= arr[i];//C
        }

        for (int num : arr) {
            temp ^= num;//A^B
        }

//        int div = 1;
//
//        while ((div&temp) == 0) {
//            div <<= 1;
//        }
        System.out.println(temp);
        int div = temp & ~(temp - 1);

        boolean a = false, b = false;
        for (int num : arr) {
            if ((num & div) == 1) {
                if (a) {
                    res[0] ^= num;//部分 ^C
                } else {
                    res[0] = num;
                    a = true;
                }
            } else {
                if (b) {
                    res[1] ^= num;//部分 ^C
                } else {
                    res[1] = num;
                    b = true;
                }
            }
        }


        for (int i = 1; i < arr.length-1; i++) {
            if ((i & div) == 1) {
                res[0] ^= i;
            } else {
                res[1] ^= i;
            }
        }

        return res;
    }

    public static int[] fun5(int[] num) {
        int[] res = new int[2];
        int bit_flag = 0;
//		* 设所求值为 A,B 其它异或和为C
//                * A^A^B^B^C=C
//                * A^B^C
//                * A^B
        //C
        for (int n : num) {
            bit_flag ^= n;
        }
        //(A^B^C)^C=A^B
        for (int i = 1; i < num.length - 1; i++) {
            bit_flag ^= i;
        }
        //这样就得到了那两个重复出现的整数的异或结果 x。接下来主要是想办法把它们两给区分开来，对于异或结果x，它的二进制表示有0和1构成，由异或的性质可知，二进制表示的x中那些出现0的位是两个重复数对应位置均为1或者0的结果，而出现1的位则只有一种可能：两个数对应位置一个是0，一个是1。借助这个特点，我们就可以选取一个特定的位置（x的那个位置是1）把原来的数组分成两个部分，部分I对应那个特定位置为1的数，部分II对应那个特定位置为0的数，这样就把问题转化为：在每个部分查找一个重复出现的数字。


        //根据bit_flag二进制中最右边的1将数组中的整数划分成两个部分
        int division_bit = bit_flag & ~(bit_flag - 1);

//        int div = 1;
//
//        while ((div & bit_flag) != 1) {
//            div <<= 1;
//        }
        System.out.println(division_bit);
//        int division_bit = div;

        int a = 0;//部分I的xor结果
        int b = 0;//部分II的xor结果
        for (int i = 0; i < num.length; i++) {
            if ((num[i] & division_bit) == 1)
                a ^= num[i];
            else
                b ^= num[i];
        }
        for (int i = 1; i < num.length - 1; i++) {
            if ((i & division_bit) == 1)
                a ^= i;
            else
                b ^= i;
        }

        res[0] = num[a];
        res[1] = num[b];

        return res;
    }

    public static int[] dup2(int[] arr) {
        int[] res = new int[2];
        int temp = arr[0];

        for (int i = 1; i < arr.length; i++) {
            temp ^= arr[i];
        }

        int div = 1;

        while ((div & temp) != 1) {
            div <<= 1;
        }

        boolean a = false, b = false;

        for (int num : arr) {
            if ((num & div) == 1) {
                if (a) {
                    res[0] ^= num;
                } else {
                    res[0] = num;
                    a = true;
                }
            } else {
                if (b) {
                    res[1] ^= num;
                } else {
                    res[1] = num;
                    b = true;
                }
            }
        }


        return res;
    }


    public static void main2(String[] args) {
        int n, m = 0;
        Scanner sc = new Scanner(System.in);
        n = sc.nextInt();
        m = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        Arrays.sort(arr);
        int[] nums = new int[m];

        for (int i = 0; i < m; i++) {
            nums[i] = sc.nextInt();
        }
        for (int i = 0; i < m; i++) {
            int target = nums[i];
            int left = target - 1;
            int right = target + 1;
            if (target < arr[0]) {
                System.out.println(arr[0]);
                continue;
            }
            if (target > arr[arr.length - 1]) {
                System.out.println(arr[arr.length - 1]);
                continue;
            }
            while (true) {
                if (search(arr, target) != -1) {
                    System.out.println(arr[search(arr, target)]);
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

    public static int search(int[] nums, int target) {
        int pivot, left = 0, right = nums.length - 1;
        int res = 0;
        while (left <= right) {
            pivot = left + (right - left) / 2;
            if (nums[pivot] == target) return pivot;
            if (target < nums[pivot]) {
                right = pivot - 1;
                res = right;
            } else {
                left = pivot + 1;
                res = left;
            }
        }
        return res;
    }
}
