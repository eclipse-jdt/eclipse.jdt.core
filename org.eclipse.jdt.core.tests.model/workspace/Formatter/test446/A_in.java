class Foo
{
    public static final Foo KABOOM = new Foo();

    private Foo() {
    }

    public Foo blowup() {
        return(Foo.KABOOM);
    }
}