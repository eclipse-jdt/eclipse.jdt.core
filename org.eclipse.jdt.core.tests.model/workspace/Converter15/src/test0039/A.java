package test0039;

@Foo({A, B, C, D})
@Bar(A.B)
public @interface A {
    Object[] bar();
}