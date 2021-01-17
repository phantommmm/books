import java.util.HashMap;
import java.util.Map;

/**
 * 最长不含重复子串
 */
public class lengthOfLongestSubstring {
    public static void main(String[] args) {
        lengthOfLongestSubstring lengthOfLongestSubstring = new lengthOfLongestSubstring();
        lengthOfLongestSubstring.fun("abcabcbb");
    }

    public void fun(String str) {
        //统计字符最后出现的索引
        Map<Character, Integer> map = new HashMap<>();
        int res = 0;
        int i = -1;//j从0开始 0开始字符为1 0--1=1
        for (int j = 0; j < str.length(); j++) {
            char c = str.charAt(j);
            if (map.containsKey(c)) {
                //更新左指针
                i = Math.max(i, map.get(c));
            }
            map.put(c, j);
            res = Math.max(res, j - i);
        }
        System.out.println(res);
    }
}
