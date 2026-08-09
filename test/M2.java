package test;

public class M2 {

    static class Parent {
        int getValue() {
            return 10;
        }
    }

    static class Child extends Parent {
        public int x;
        Child(int x) {

        }
        int getValue() {
            return 20;
        }

        int test() {
            return super.getValue();
        }
    }

    public static int main(String[] args) {
        Child c = new Child(3);
        return c.test();
    }
}