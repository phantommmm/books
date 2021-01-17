import java.util.Arrays;

/**
 * 验证合法ip地址
 * 如果是有效的 IPv4 地址，返回 "IPv4" ；
 * 如果是有效的 IPv6 地址，返回 "IPv6" ；
 * 如果不是上述类型的 IP 地址，返回 "Neither" 。
 *
 * 输入：IP = "172.16.254.1"
 * 输出："IPv4"
 * 输入：IP = "2001:0db8:85a3:0:0:8A2E:0370:7334"
 * 输出："IPv6"
 */
public class validIPAddress {
    public static void main(String[] args) {
        String ip1 = "172.16.254.1";
        String ip2="2001:0db8:85a3:0:0:8A2E:0370:7334";
        String ip3="2001:0db8:85a3:0:0:8A2E:0370:7334:";
        validIPAddress validIPAddress=new validIPAddress();
        System.out.println(validIPAddress.fun(ip1));
        System.out.println(validIPAddress.fun(ip2));
        System.out.println(validIPAddress.fun(ip3));
    }

    public String fun(String ip) {
        if (ip.indexOf('.')!=-1){
            return validIPv4(ip);
        }
        if (ip.indexOf(':')!=-1){
            return validIPv6(ip);
        }
        return "Neigher";
    }

    public String validIPv4(String ip) {
        //注意直接使用.不行，得转义字符\\. -1是最后的.也会分割出空值进数组
        String[] nums = ip.split("\\.",-1);

        if (nums.length!=4){
            return "Neither";
        }
        for (String num : nums) {
            //1-3位
            if (num.length() == 0 || num.length() > 3) {
                return "Neither";
            }
            //不能以0开头
            if (num.charAt(0) == '0' && num.length() != 1) {
                return "Neither";
            }
            for (Character c : num.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return "Neigher";
                }
            }
            if (Integer.parseInt(num) > 255) {
                return "Neigher";
            }
        }
        return "IPv4";
    }

    public String validIPv6(String ip) {
        //-1是最后的.也会分割出空值进数组
        String[] nums=ip.split(":",-1);
        String dict="0123456789abcdefABCDEF";

        if (nums.length!=8){
            return "Neither";
        }

        for (String num:nums){
            //1-4位
            if (num.length()==0||num.length()>4){
                return "Neigher";
            }
            //0-9 a-f A-F
            for (char c:num.toCharArray()) {
                if (dict.indexOf(c)==-1){
                    return "Neigher";
                }
            }
        }
        return "IPv6";
    }

}
