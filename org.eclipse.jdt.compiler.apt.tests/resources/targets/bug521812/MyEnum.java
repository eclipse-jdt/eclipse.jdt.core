package targets.bug521812;

@org.eclipse.jdt.compiler.apt.tests.annotations.Type
enum MyEnum {
    A(1), B(2);

    MyEnum(int v) { this.v = v; }
    public int value() { return v; }
    final int v;
}