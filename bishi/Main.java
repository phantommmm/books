import java.util.*;

public class Main {

    private Stack<Integer> stack;
    private Stack<Integer> min_stack;


    public Main() {
        stack = new Stack<>();
        min_stack = new Stack<>();
    }

    public static int solution(int N) {
        // write your code in Java SE 8
        if(N>8000||N<-8000){
            return -1;
        }

        List<Integer> list=new ArrayList<>();
        //标志位
        int flag=0,done=0;
        int res=0,idx=0;

        //区分正负数
        flag=(N>=0)?1:-1;
        N=Math.abs(N);

        //依次添加到集合
        while (N>=10){
            list.add(0,N%10);
            N/=10;
        }
        list.add(0,N);

        //从最高位开始遍历
        for (int i=0;i<list.size();i++){
            //正数 并且当前位小于5，则在当前位添加5 并且后面全部后移
            if (flag==1&&list.get(i)<5){
                list.add(i,5);
                done=1;
                break;
            }
            //负数
            else if (flag==-1){
                //首位大于5，则在当前位添加5 并且后面全部后移
                if (i==0&&list.get(i)>5){
                    list.add(0, 5);
                    done=1;
                    break;
                }
                //当前位小于5，下一位大于5 在中间位添加5
                else if (i+1<list.size()&&list.get(i)<=5&&list.get(i+1)>5){
                    list.add(i+1,5 );
                    done=1;
                    break;
                }
            }
        }
        //全都不满足 最后一位添加
        if (done==0){
            list.add(5);
        }

        while (idx<list.size()){
            res+=list.get(idx)*Math.pow(10, list.size()-idx-1);
            idx++;
        }
        return flag*res;
    }

    public static void main(String[] args) {
        System.out.println(solution(268));
        System.out.println(solution(670));
        System.out.println(solution(0));
        System.out.println(solution(-999));
        System.out.println(solution(-27));
        System.out.println(solution(-2));

    }
    public static void main4(String[] args) {
        Scanner in=new Scanner(System.in);
        while (in.hasNext()){
            String str=in.nextLine();
            char[] arr=str.toCharArray();
            Map<Character,Integer> map=new LinkedHashMap<>();

            for (char c:arr){
                if (!map.containsKey(c)){
                    map.put(c,1);
                }else{
                    map.put(c,map.get(c)+1);
                }
            }
            Collection<Integer> collection=map.values();
            int idx=Collections.min(collection);
            StringBuilder sb=new StringBuilder();
            for (char c:arr){
                if (map.get(c)!=idx){
                    sb.append(c);
                }
                System.out.println(sb.toString());
            }
        }
    }

    public static void main3(String[] args) {
        Main main = new Main();
        int n = 0;
        Scanner in = new Scanner(System.in);
        n = in.nextInt();
        List<String> list = new ArrayList<>();
        for (int i=0;i<10;i++){
            list.add(in.next());
        }
        System.out.println(list.toString());
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i);
            switch (str) {
                case "push": {
                    i++;
                    main.push(Integer.valueOf(list.get(i)));
                }
                case "pop":{
                    main.pop();
                }
                case "top":{
                    System.out.println(main.top());
                }
                case "getMin":{
                    System.out.println(main.getMin());
                }
            }
        }
    }

    public void push(int x) {
        stack.push(x);
        if (min_stack.isEmpty() || x <= min_stack.peek()) {
            min_stack.push(x);
        }
    }

    public void pop() {
        if (stack.pop().equals(min_stack.peek())) {
            min_stack.pop();
        }
    }

    public int top() {
        return stack.peek();
    }

    public int getMin() {
        return min_stack.peek();
    }

    public static void main1(String[] args) {
        int n = 0;
        Scanner in = new Scanner(System.in);
        n = in.nextInt();
        System.out.println(nthUgly(n));
    }

    public static void main2(String[] args) {
        String s = "";
        Scanner in = new Scanner(System.in);
        s = in.next();
        System.out.println(fun(s));
    }

    public static String fun(String s) {
        int[] num = new int[26];
        for (int i = 0; i < num.length; i++) {
            num[i] = -1;
        }
        for (int i = 0; i < s.length(); i++) {
            num[s.charAt(i) - 'a']++;
        }
        int min = 0;
        for (int i = 0; i < s.length(); i++) {
            if (num[s.charAt(i) - 'a'] <= min) {
                min = num[s.charAt(i) - 'a'];
            }
        }
        for (int i = 0; i < s.length() && i >= 0; ) {
            if (num[s.charAt(i) - 'a'] == min) {
                if (i > 0) {
                    if (i < num.length - 1) {
                        s = s.substring(0, i) + s.substring(i + 1);
                    } else {
                        s = s.substring(0, i - 1);
                    }
                } else {
                    s = s.substring(i + 1);
                }
            } else {
                i++;
            }
        }
        return s;
    }

    public static int nthUgly(int n) {
        int p2 = 0, p3 = 0, p5 = 0;
        int[] dp = new int[n];
        dp[0] = 1;
        for (int i = 1; i < n; i++) {
            dp[i] = Math.min(dp[p2] * 2, Math.min(dp[p3] * 3, dp[p5] * 5));
            if (dp[i] == dp[p2] * 2) p2++;
            if (dp[i] == dp[p3] * 3) p3++;
            if (dp[i] == dp[p5] * 5) p5++;
        }
        return dp[n - 1];
    }

}
