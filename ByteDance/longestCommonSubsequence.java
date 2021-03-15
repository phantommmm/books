/**
 * 最长公共子序列
 * 输入：text1 = "abcde", text2 = "ace"
 * 输出：3
 * 解释：最长公共子序列是 "ace"，它的长度为 3。
 */

/**
 * 输入：
 * A: [1,2,3,2,1]
 * B: [3,2,1,4,7]
 * 输出：3
 * 解释：
 * 长度最长的公共子数组是 [3, 2, 1] 。
 */
public class longestCommonSubsequence {

    //注意！ 子字符串和数组可以相互转化 方法都一样
    public static void main(String[] args) {
        String text1="12321";
        String text2="321478";
        int[] A={1,2,3,2,1};
        int[] B={3,2,1,4,7,8};
        String strA="12321";
        String strB="321478";
        longestCommonSubsequence longestCommonSubsequence=new longestCommonSubsequence();
        //System.out.println(longestCommonSubsequence.fun(strA,strB));
//        System.out.println(longestCommonSubsequence.fun2(strA,strB));
//        System.out.println(longestCommonSubsequence.fun4(A,B));
//        int[] nums={3,4,3,3};
//        System.out.println(longestCommonSubsequence.singleNumber(nums));
        ThreadLocal<Integer> integerThreadLocal=new ThreadLocal<>();
        ThreadLocal<String> stringThreadLocal=new ThreadLocal<>();
        integerThreadLocal.set(1);
        integerThreadLocal.set(2);
        stringThreadLocal.set("str");
        System.out.println(integerThreadLocal.get());
    }

    public int singleNumber(int[] nums) {
        if(nums.length==0||nums==null) return 0;
        int res=0;
        for(int i=0;i<32;i++){
            int index=1<<i;
            int count=0;//记录1个数
            for(int num:nums){
                //index只有对应位为1 其它为0
                if((num&index)!=0){
                    count++;
                }
            }
            if(count%3==1){
                System.out.println(res);
                System.out.println(index);
                System.out.println(count);
                res|=index;
            }
        }
        return res;
    }

    //最长公共子字符串/最长公共子数组 滑动窗口 (n+m)*min(n,m)
    public int fun4(int[] A,int[] B){
        int n=A.length;
        int m=B.length;
        int res=0;

        //左往右
        for (int i=0;i<n;i++){
            int len=Math.min(m,n-i);
            int maxLen=maxLength(A,B,i,0,len);
            res=Math.max(res,maxLen);
        }
        //右往左
        for (int i=0;i<m;i++){
            int len=Math.min(n,m-i);
            int maxLen=maxLength(A,B,0,i,len);
            res=Math.max(res,maxLen);
        }

        return res;
    }

    //addA addB 为比较数组的起始坐标
    //返回子数组中 最大公共长度
    private int maxLength(int[] A,int[] B,int addA,int addB,int len){
        int res=0,k=0;
        for (int i=0;i<len;i++){
            if (A[addA+i]==B[addB+i]){
                k++;
            }else{
                k=0;
            }
            res=Math.max(res,k);
        }
        return res;
    }

    //最长公共子字符串 动态规划 min(m,n)
    public int fun3(String text1,String text2){
        int n=text1.length();
        int m=text2.length();
        int[] dp=new int[m+1];
        int res=0;
        //二维dp 因为要和前面值比较 所以从i+1/j+1开始
        for(int i=0;i<n;i++){
            char c1=text1.charAt(i);
            //因为dp数组后面的值会依赖前面的值，而前面的值不依赖后面的值，
            // 前面的值修改会对后面的值有影响，所以这里要使用倒序的方式。
            for (int j=m;j>0;j--){
                char c2=text2.charAt(j);
                if (c1==c2){
                    dp[j]=dp[j-1]+1;
                    res=Math.max(res,dp[j]);
                }else{
                    dp[j]=0;
                }
            }
        }
        return res;
    }

    //最长公共子字符串 动态规划 m*n
    public int fun2(String text1,String text2){
        int n=text1.length();
        int m=text2.length();
        int[][] dp=new int[n+1][m+1];
        int res=0;
        //二维dp 因为要和前面值比较 所以从i+1/j+1开始
        for(int i=0;i<n;i++){
            char c1=text1.charAt(i);
            for (int j=0;j<m;j++){
                char c2=text2.charAt(j);
                //字符相同 则取左上角+1 即都减少一个字符的结果+1
                if (c1==c2){
                    dp[i+1][j+1]=dp[i][j]+1;
                    //子串 结果不一定最后 所以设res比较
                    res=Math.max(res,dp[i+1][j+1]);
                }
            }
        }
        return res;
    }

    //最长公共子序列
    public int fun(String text1,String text2){
        int n=text1.length();
        int m=text2.length();
        int[][] dp=new int[n+1][m+1];
        //二维dp 因为要和前面值比较 所以从i+1/j+1开始
        for(int i=0;i<n;i++){
            char c1=text1.charAt(i);
            for (int j=0;j<m;j++){
                char c2=text2.charAt(j);
                //字符相同 则取左上角+1 即都减少一个字符的结果+1
                if (c1==c2){
                    dp[i+1][j+1]=dp[i][j]+1;
                }else{
                    //字符不同 则取左/上 的较大值
                    dp[i+1][j+1]=Math.max(dp[i][j+1],dp[i+1][j]);
                }
            }
        }
        return dp[n][m];
    }

}
