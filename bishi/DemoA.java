public class DemoA {
    private String a1;
    private static String a5;

    public DemoA(String str){
        a5=str;
    }

    public static void main(String[] args) {
//        String a="a",b="b",c="c";
//        String str=a+b+c;
        String c="abc";
        String abc=new String("ab")+new String("c");
        System.out.println(abc.intern()==abc);
    }

    public String getA3(){
        String a3="abc";
        return a3;
    }
}

class DemoB{
    private String a2;

    public void setA2(String str){
        a2=str;
    }

    public String getA2(){
        return a2;
    }
}
