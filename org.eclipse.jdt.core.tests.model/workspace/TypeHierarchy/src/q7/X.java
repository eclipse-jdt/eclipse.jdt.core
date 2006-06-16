package q7;
public class X {
    public class Member {
    }
}
class Y {
    void foo(X arg) {
        arg.new Member() {
        };
    } 
}