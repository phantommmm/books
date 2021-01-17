/**
 * 最长回文子串
 */
public class longestPalindrome {

    public static void main(String[] args) {
        String str="abc1234321ab";
        longestPalindrome l=new longestPalindrome();
        //System.out.println(l.fun("a"));
        System.out.println(l.fun(str));
    }


    public String fun(String str){
        int len=str.length();
        if (len<2) return str;
        String res=str.substring(0,1);
        for (int i=0;i<len-1;i++){
            String sub1=help(str,i,i);
            String sub2=help(str,i,i+1);
            sub1=sub1.length()>sub2.length()?sub1:sub2;
            res=res.length()>sub1.length()?res:sub1;
        }

        return res;
    }

    private String help(String str,int left,int right){
        int i=left,j=right;

        while (i>=0&&j<str.length()){
            if (str.charAt(i)==str.charAt(j)){
                i--;
                j++;
            }else{
                break;
            }
        }

        return str.substring(i+1,j);
    }
}
