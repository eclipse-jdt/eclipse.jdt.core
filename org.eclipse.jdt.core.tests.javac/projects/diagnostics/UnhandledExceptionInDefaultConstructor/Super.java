package UnhandledExceptionInDefaultConstructor;

public class Super {
    Super() throws Exception {
        throw new Exception("Exp");
    }
}