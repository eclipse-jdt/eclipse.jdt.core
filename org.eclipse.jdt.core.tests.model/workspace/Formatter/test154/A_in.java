package test1;
import java.util.Vector;
public class E {
    private class EInner {
        public int inner(int i) {
        }
    }
    private int fField1;
    private int fField2;
    public void foo1() {
        fField1 = fField2;
        if (fField1 == 0) {
            fField2++;
        }
        EInner inner = new EInner();
    }
    public int foo1(int i) {
    }
}