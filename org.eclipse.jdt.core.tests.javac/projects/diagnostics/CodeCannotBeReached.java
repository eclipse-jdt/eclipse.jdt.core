public class CodeCannotBeReached {
    public void testCodeCannotBeReached() {
        return;
        // compiler.err.unreachable.stmt -> CodeCannotBeReached(536871073)
        String reach = "";
    }
}
