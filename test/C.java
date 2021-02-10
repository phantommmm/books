public  class C extends B{
    int a = 3;

    @Override
    void f1() {
        System.out.println("C.f1()");
    }

    void f3() {
        System.out.println("C.f3()");
    }

    static void f4(){
        System.out.println("C.f4()");
    }

    public static void main(String[] args) {

        B b = new C();// 父类引用指向子类对象
        b.f1();// 子类覆盖了该方法，所以父类引用调用新方法
        b.f2();// 子类未覆盖该方法，所以父类引用调用旧方法
        // b.f3();此行去掉注释会报错，父类引用不能访问子类新定义方法
        B.f4();
        System.out.println(b.a);// 子类覆盖了该属性，但父类引用仍旧访问旧属性
        System.out.println(b.b);// 子类未覆盖该属性，父类访问旧属性

        System.out.println();

        C c = new C();// 子类引用指向自身对象
        c.f1();// 子类覆盖了父类方法，所以调用新方法
        c.f2();// 子类未覆盖父类方法，所以调用旧方法
        c.f3();// 子类调用自己新定义的方法
        C.f4();
        System.out.println(c.a);// 子类覆盖了该属性，所以访问新属性
        System.out.println(c.b);// 子类未覆盖该属性，所以访问旧属性
    }
}
