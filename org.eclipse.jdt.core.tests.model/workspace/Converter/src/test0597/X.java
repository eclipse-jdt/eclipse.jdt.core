package test0597;

import junit.framework.Protectable;
import junit.framework.Test;

public class X {
    void m(Test t) {
        if (t instanceof Protectable) {}
    }
}