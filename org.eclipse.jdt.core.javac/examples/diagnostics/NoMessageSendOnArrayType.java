
public class NoMessageSendOnArrayType {
    public void testNoMessageSendOnArrayType() {
        String[] test = {"1", "2"};
        // compiler.err.cant.resolve.location.args -> NoMessageSendOnArrayType(67108980)
        int size = test.size();
    }
}
