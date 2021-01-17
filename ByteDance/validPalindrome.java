/**
 * 最多减少一次字符为回文串
 */
public class validPalindrome {

    public static void main(String[] args) {
        validPalindrome v=new validPalindrome();
        String str="abca";
        System.out.println(v.fun(str));
    }

    public boolean fun(String str){
        int low=0,hign=str.length()-1;
        while (low<hign){
            char c1=str.charAt(low);
            char c2=str.charAt(hign);
            if (c1==c2){
                low++;
                hign--;
            }else{
                boolean flag1=true,flag2=true;
                for (int i=low,j=hign-1;i<j;i++,j--){
                    char c3=str.charAt(i);
                    char c4=str.charAt(j);
                    if (c3!=c4){
                        flag1=false;
                        break;
                    }
                }
                for (int i=low+1,j=hign;i<j;i++,j--){
                    char c3=str.charAt(i);
                    char c4=str.charAt(j);
                    if (c3!=c4){
                        flag2=false;
                        break;
                    }
                }
                return flag1||flag2;
            }
        }
        return true;
    }
}
