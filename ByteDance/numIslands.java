/**
 * 岛屿数量
 * 输入:
 * [
 * ['1','1','0','0','0'],
 * ['1','1','0','0','0'],
 * ['0','0','1','0','0'],
 * ['0','0','0','1','1']
 * ]
 * 输出: 3
 */
public class numIslands {
    int n, m;


    public int fun(char[][] grid) {
        int res=0;
        if (grid.length == 0) {
            return 0;
        }

        this.n=grid.length;
        this.m=grid[0].length;

        for (int i=0;i<n;i++){
            for (int j=0;j<m;j++){
                //首先找到岛
                if ('1'==grid[i][j]){
                  res++;
                  dfs(i,j,grid);
                }
            }
        }
        return res;
    }

    private void dfs(int i, int j, char[][] grid) {
        //终止条件
        if (i<0||i>n-1||j<0||j>m-1||'0'==grid[i][j]){
            return ;
        }
        //将同一块的'1'变成'0'
        grid[i][j]='0';
        dfs(i+1,j,grid);
        dfs(i-1,j,grid);
        dfs(i,j+1,grid);
        dfs(i,j-1,grid);
    }
}
