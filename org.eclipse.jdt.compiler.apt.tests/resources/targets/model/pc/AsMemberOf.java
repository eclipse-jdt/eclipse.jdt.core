/*******************************************************************************
 * Copyright (c) 2015 Walter Harley and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package targets.model.pc;

/**
 * Bug 382590 says that Types.asMemberOf(x, y) should work when x is a subclass
 * of the class containing y. 
 * 
 * See TypeUtilsProc.java, which gets exercised by ModelUtilTests.testTypes*().
 */
public class AsMemberOf<T> {
    private T f;
    protected T m(T[] t1, T t2) { return f; }
    E<Integer> e = new E<Integer>();
    
    // Type parameter 'T' of static class is unrelated to 'T' of containing class
    private static class C<T> {
        T x() { return null; }
    }
    
    // Type parameter 'T' of inner class is inherited from the containing class
    private class D {
        T x() { return null; }
    }
    
    // Both container and contained are parameterized
    class E<T2> {
        T x() { return null; }
        T2 y() { return null; }
    }
    
    // Need this so that compiler doesn't complain about unused private members.
    // Members are private to verify that asMemberOf() does not consider visibility,
    // even when accesed through a subclass; this is not explicitly specified, but 
    // is true for javac 1.6.
    T publicize() {
        return (m(null, null) == null) ? new C<T>().x() : new D().x();
    }
}

interface IAsMemberOf<T> {
    // Types.asMemberOf() should find members in superinterfaces
    void m2();
}

abstract class AsMemberOfSub extends AsMemberOf<Long> implements IAsMemberOf<Long> {
    // m2 not implemented; thus class must be abstract
    // f2() intentionally not defined; negative test
}

