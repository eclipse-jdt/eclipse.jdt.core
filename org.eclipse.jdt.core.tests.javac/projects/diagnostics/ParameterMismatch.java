
public class ParameterMismatch {

    private String message;

    private void setMessage(String message) {
        this.message = message;
    }

    private void testMethodParameterMatch() {
        // compiler.err.cant.apply.symbol -> ParameterMismatch(67108979)
        this.setMessage();
    }

    void m(int i1) {}
    void m(int i1, int i2) {}
    
    ParameterMismatch() {
        // compiler.err.cant.apply.symbols -> ParameterMismatch(67108979)
        this.m(); 
    }
}
