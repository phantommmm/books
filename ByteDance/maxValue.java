public class maxValue {

    public static void main(String[] args) {
        int[][] arr={{1,2,5},{3,2,1}};
        maxValue(arr);
    }

    public static int maxValue(int[][] grid) {
        int n=grid.length,m=grid[0].length;
        int[][] dp=new int[n][m];
        dp[0][0]=grid[0][0];

        for(int i=1;i<n;i++){
            dp[i][0]=grid[i][0]+grid[i-1][0];
        }
        for(int j=1;j<m;j++){
            dp[0][j]=grid[0][j]+grid[0][j-1];
        }

        for(int i=1;i<dp.length;i++){
            for(int j=1;j<dp[0].length;j++){
                dp[i][j]=grid[i][j]+Math.max(dp[i-1][j],dp[i][j-1]);
            }
        }


        return dp[n-1][m-1];
    }
}
