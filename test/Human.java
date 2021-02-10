public class Human {

    public void say(Human human) {
        System.out.println("Human");
    }

//    public void say(Woman human) {
//        System.out.println("Woman");
//    }
//
    public void say(Man man) {
        System.out.println("Man");
    }


    public static void main(String[] args) {
        Human human = new Human();
        Man man=new Man();
        Human h=new Man();
//        human.say(man);
        human.say(human);
        human.say(man);
        human.say(h);
    }
}

class Man extends Human {

//    @Override
//    public void say() {
//        System.out.println("mmmm");
//    }
//
//    public void say2() {
//        System.out.println("Man man");
//    }
}

class Woman extends Human {

}