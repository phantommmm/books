//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.*;
//
//public class Demo {
//
//
//
//    public static void main1(String[] args) {
//        Scanner in=new Scanner(System.in);
//        List<Integer> list=new ArrayList<>();
//        list.toArray(new Integer[0]);
//               while(in.hasNextInt()){
//            int num=in.nextInt();
//            list.add(fun1(num));
//        }
//        System.out.println(list.toString());
//        String[] strs=new String[list.size()];
//        for (int i=0;i<list.size();i++){
//            String str=String.valueOf(list.get(i));
//            char c1=str.charAt(str.length()-1);
//            char c2=str.charAt(str.length()-2);
//            strs[i]=c1+c2+"";
//        }
//
//        String s=String.valueOf(list.get(list.size()-1));
//        StringBuilder sbb=new StringBuilder();
//        sbb.append(strs[list.size()-1]);
//        sbb.append(s.substring(2));
//        System.out.print(sbb.toString()+" ");
//
//        for (int i=1;i<list.size()-1;i++){
//            String str=String.valueOf(list.get(i));
//            StringBuilder sb=new StringBuilder();
//            sb.append(strs[i]);
//            sb.append(str.substring(2));
//            System.out.print(sb.toString()+" ");
//        }
//
//    }
//
//    public static void main2
//            (String[] args) {
//        Scanner in=new Scanner(System.in);
//        List<Integer> list=new ArrayList<>();
//        while(in.hasNextInt()){
//            list.add(swapbit(in.nextInt()));
//        }
//        Integer[] arr=list.toArray(new Integer[0]);
//
//        for (int i=0;i<arr.length;i++){
//            System.out.println(Math.abs(arr[i]));
//        }
//
//    }
//    private static int swapbit(int n){
//        int a=n&0b1010_1010_1010_1010_1010_1010_1010_1010;
//        int b=n&0b0101_0101_0101_0101_0101_0101_0101_0101;
//        a=a>>1;
//        b=b<<1;
//        return a|b;
//    }
//
//    private static Integer[] move(Integer[] arr){
//        if (arr==null||arr.length==0){
//            return arr;
//        }
//        int tail=arr[0]&3;
//        arr[0]=arr[0]>>2;
//        int front=tail<<30;
//
//        for (int i=1;i<arr.length;i++){
//            tail=arr[i]&3;
//            arr[i]=arr[i]>>2;
//            arr[i]=arr[i]|front;
//            arr[i]=Math.abs(arr[i]);
//            front=tail<<30;
//        }
//        arr[0]=arr[0]|front;
//        return arr;
//    }
//
//
//
//    private static int fun2(int num1,int num2){
//        int temp=num1;
//        String str1=String.valueOf(num1);
//        String str2=String.valueOf(num2);
//        StringBuilder sb= new StringBuilder();
//        if (num1>>2<0){
//            System.out.print(str1.substring(0,str1.length()-3)+" ");
//            char c1=str1.charAt(str1.length()-1);
//            char c2=str1.charAt(str1.length()-2);
//            str2+=c1+c2;
//            return Integer.valueOf(str2);
//        }else{
//            System.out.println(num1>>2);
//            return num2;
//        }
//
//    }
//
//    private static int fun1(int num){
//        int a;
//        int ch=0;
//        int i=0;
//        while (num!=0){
//            a=num%2;
//            num/=2;
//            ch+=a*(Math.pow(10, i));
//            i++;
//        }
//        String str=String.valueOf(ch);
//        int j=0;
//        StringBuilder sb=new StringBuilder();
//        while (j<str.length()){
//            char c1=str.charAt(j);
//            j++;
//            if (j<str.length()){
//                char c2=str.charAt(j);
//                sb.append(c2);
//            }
//            j++;
//            sb.append(c1);
//        }
//        return towToTen(sb.toString());
//    }
//    private static int towToTen(String two){
//        int s=Integer.valueOf(two);
//        int sum=0;
//        int i=0;
//        while(s!=0){
//            sum=(int) (sum+s%10*(Math.pow(2, i)));
//            s/=10;
//            i++;
//        }
//        return sum;
//    }
//
//
//
//
////    public static void main(String[] args) {
////        Scanner in=new Scanner(System.in);
////        List<Integer> listx=new ArrayList<>();
////        List<Integer> listy=new ArrayList<>();
////        String str=in.next();
////        boolean flag=false;
////        for (char c:str.toCharArray()){
////            if (c=='['||c==','){
////                continue;
////            }
////            if (c==']'&&!flag){
////                flag=true;
////                continue;
////            }
////            if (c==']'&&flag){
////                continue;
////            }
////            if (c-'0'>=1&&c-'0'<=100) {
////                if (!flag){
////                    listx.add(c-'0');
////                }else{
////                    listy.add(c-'0');
////                }
////            }else{
////                System.out.println(0);
////                return;
////            }
////        }
////        List<Integer> list=new ArrayList<>();
////        for (int i=0;i<listx.size();i++){
////            int times=listx.get(i);
////            int temp=listy.get(i);
////            while(times>0){
////                list.add(temp);
////                times--;
////            }
////        }
////
////        int res=0;
////        for (int i=0;i<list.size();i++){
////            int left=i;
////            int curHeight=list.get(i);
////            while (left>0&&list.get(left-1)>=curHeight){
////                left--;
////            }
////            int right=i;
////            while (right<list.size()-1&&list.get(right+1)>=curHeight){
////                right++;
////            }
////            int width=right-left+1;
////            res=Math.max(res, width*curHeight);
////        }
////        System.out.println(res);
////    }
//
//
//
//
//
////    public static void main(String[] args) {
////        Scanner in=new Scanner(System.in);
////        int n=0;
////        n=in.nextInt();
////        int[][] arr=new int[n][3];
////        int res=0;
////        for (int i=0;i<n;i++){
////            arr[i][0]=in.nextInt();
////            arr[i][1]=in.nextInt();
////            arr[i][2]=in.nextInt();
////        }
////
////        int start=0,end=Integer.MAX_VALUE;
////        for (int i=0;i<n;i++){
////            if (start<=arr[i][0]&&end>=arr[i][1]){
////                res+=arr[i][2];
////                start=arr[i][0];
////                end=arr[i][1];
////            }
////        }
////        System.out.println(res);
////    }
//
//    public static  <T extends  Comparable<? super T>> void reverse(T[] arr){
//        for (int i=0;i<arr.length-1;i++){
//            for (int j=0;j<arr.length-i-1;j++){
//                if (arr[j+1].compareTo(arr[j])<0){
//                    T temp=arr[j];
//                    arr[j]=arr[j+1];
//                    arr[j+1]=temp;
//                }
//            }
//        }
//
//        int start=0;
//        int end=arr.length-1;
//        while (true){
//            if (start>=end){
//                break;
//            }
//            T temp=arr[start];
//            arr[start]=arr[end];
//            arr[end]=temp;
//
//            start++;
//            end--;
//        }
//    }
//
//
//
//
////    public static void main(String[] args) {
////        List<Integer> list=new ArrayList<>();
////        List<Integer> list2=new ArrayList<>();
////        Integer[] arr=new Integer[2]; //声明为Integer
////        arr=list.toArray(new Integer[0]);
////
////        int[] arr2=new int[2];
////        arr2=list2.stream().mapToInt(Integer::valueOf).toArray();
////
////    }
//
//    static class Profit implements Comparable<Profit> {
//        int x;
//
//        public Profit(int x, int y) {
//            this.x = x;
//            this.y = y;
//        }
//
//        @Override
//        public String toString() {
//            return "Profit{" +
//                    "x=" + x +
//                    ", y=" + y +
//                    '}';
//        }
//
//        int y;
//
//        @Override
//        public int compareTo(Profit o) {
//            int result = 0;
//            if (x == o.x) {
//                result = y > o.y ? 1 : -1;
//            } else {
//                result = x > o.x ? -1 : 1;
//            }
//            return result;
//        }
//    }
//
////    public static void main(String[] args) {
////        Profit p1=new Profit(1, 2);
////        Profit p2=new Profit(1,5);
////        Profit[] profits=new Profit[2];
////        profits[0]=p1;
////        profits[1]=p2;
////        Arrays.sort(profits);
////        System.out.println(Arrays.toString(profits));
////    }
////
//
//
//
//
//    public static boolean isPrime(long N) {
//        if (N < 2) {
//            return false;
//        }
//        int R = (int) Math.sqrt(N);
//        for (int d = 2; d <= R; ++d) {
//            if (N % d == 0) return false;
//        }
//        return true;
//    }
//
////    public static long reverse(long N) {
////        long ans = 0;
////        while (N > 0) {
////            ans = 10 * ans + (N % 10);
////            N /= 10;
////        }
////        return ans;
////    }
//
////    private static void quickSort1(int[] arr, int low, int high) {
////        if (low < high) {
////            // 找寻基准数据的正确索引
////            int index = getIndex1(arr, low, high);
////
////            // 进行迭代对index之前和之后的数组进行相同的操作使整个数组变成有序
////            //quickSort(arr, 0, index - 1); 之前的版本，这种姿势有很大的性能问题，谢谢大家的建议
////            quickSort1(arr, low, index - 1);
////            quickSort1(arr, index + 1, high);
////        }
////
////    }
////
////    private static int getIndex1(int[] arr, int low, int high) {
////        // 基准数据
////        int tmp = arr[low];
////        while (low < high) {
////            // 当队尾的元素大于等于基准数据时,向前挪动high指针
////            while (low < high && arr[high] >= tmp) {
////                high--;
////            }
////            // 如果队尾元素小于tmp了,需要将其赋值给low
////            arr[low] = arr[high];
////            // 当队首元素小于等于tmp时,向前挪动low指针
////            while (low < high && arr[low] <= tmp) {
////                low++;
////            }
////            // 当队首元素大于tmp时,需要将其赋值给high
////            arr[high] = arr[low];
////
////        }
////        // 跳出循环时low和high相等,此时的low或high就是tmp的正确索引位置
////        // 由原理部分可以很清楚的知道low位置的值并不是tmp,所以需要将tmp赋值给arr[low]
////        arr[low] = tmp;
////        return low; // 返回tmp的正确位置
////    }
//
//
//    public static void main(String[] args) {
//        Scanner in=new Scanner(System.in);
//        int n=in.nextInt();
//        boolean[] bs=new boolean[n];
//        for (int i=2;i<n;i++){
//            for(int j=i+1;j<n;j++){
//                if (j%i==0){
//                    //不是质数
//                    bs[j]=true;
//                }
//            }
//        }
//        //0 1不是质数
//        for (int i=2;i<n;i++){
//            if (!bs[i]){
//                System.out.println(i);
//            }
//        }
//    }
//
//
//
//
//    public static int commonBinarySearch(int[] arr,int key){
//        int low = 0;
//        int high = arr.length - 1;
//        int middle = 0;			//定义middle
//
//        if(key < arr[low] || key > arr[high] || low > high){
//            return -1;
//        }
//
//        while(low <= high){
//            middle = (low + high) / 2;
//            if(arr[middle] > key){
//                //比关键字大则关键字在左区域
//                high = middle - 1;
//            }else if(arr[middle] < key){
//                //比关键字小则关键字在右区域
//                low = middle + 1;
//            }else{
//                return middle;
//            }
//        }
//
//        return -1;		//最后仍然没有找到，则返回-1
//    }
//}
//class ListNode{
//    ListNode next;
//    int val;
//
//    public ListNode(int val) {
//        this.val = val;
//    }
//}