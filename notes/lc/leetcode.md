#### 复原IP地址

```
输入: "25525511135"
输出: ["255.255.11.135", "255.255.111.35"]
```

```
思路：
IP地址满足的条件：
1.3个点，4个段
2.0=<每个段<=255 0只能单独存在
3.0=<每个段长度<=3
4.IP地址要包含字符串所有
```

```
暴力法
 public List<String> restoreIpAddresses(String s) {
 		List<String> res=new ArrayList<String>();
 		int n=s.length();
 		//每段字符串长度可能为1-3所以依次遍历
 		for(int i=0;i<3;i++){
			for(int j=i+1;j<i+4;j++){
				for(int k=j+1;k<j+4;k++){
					if(i<n&&j<n&&k<n){
					//substring(0,i+1)取的子字符串是0-i最后那个不取 所以要+1
					//分别取出4段字符串
						String tmp1=s.substring(0,i+1);
						String tmp2=s.substring(i+1,j+1);
						String tmp3=s.substring(j+1,k+1);
						String tmp4=s.substring(k+1);
						//4段字符串都满足ip地址的话，添加
						if(help(tmp1)&&help(tmp2)&&help(tmp3)&&help(tmp4)){
							String str=tmp1+'.'+tmp2+'.'+tmp3+'.'+tmp4;
							res.add(str);
						}
					}
				}
			}
		}
 }

//判断每段是满足IP
private boolean help(String tmp){
if(tmp==null||tmp.length()==0||(tmp.charAt(0)=='0'&&tmp.length()>1)||tmp.length>3||Integer.parseInt(tmp)>255)
	return false;
	else return true;
}
```



### 背包

#### 初始化细节

**恰好装满背包：dp[0]=0 其它为-∞**

**无需装满背包，只需价值尽量大，则dp[0...]=0 所有初始化为0**

#### 01背包

```
// W 为背包总体积
// N 为物品数量
// weights 数组存储 N 个物品的重量
// values 数组存储 N 个物品的价值
public int knapsack(int W, int N, int[] weights, int[] values) {
    int[] dp = new int[W + 1];
    for (int i = 1; i <= N; i++) {
        int w = weights[i - 1], v = values[i - 1];
        for (int j = W; j >= w; j--) {//逆序 
                dp[j] = Math.max(dp[j], dp[j - w] + v);
        }
    }
    return dp[W];
}
```

#### 完全背包

```
for (int i = 1; i <= N; i++)//先迭代物品
    for (int j = w[i]; j <= W; j++)//顺序
        f[j] = max(f[j], f[j - w[i]] + v[i]);
```

**取组合数： dp[i]+=dp[i-nums[j]];**

**背包有顺序的：**

```
  for (int i = 1; i <= n; i++) {
        for (String word : wordDict) {   // 对物品的迭代应该放在最里层
            ...
        }
    }
```

