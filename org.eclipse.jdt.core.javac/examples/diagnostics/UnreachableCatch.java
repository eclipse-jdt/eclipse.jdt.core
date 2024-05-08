
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class UnreachableCatch {

    public void testUnreachableCatch() {
        try {
            String a = "a";
        } catch (IOException e) { // compiler.err.except.never.thrown.in.try -> UnreachableCatch(83886247)


        }
    }

    public void testInvalidCatchBlockSequence() {
        try {
            boolean success = new File("f").createNewFile();
        } catch (IOException e) {

        } catch (FileNotFoundException e) { // compiler.err.except.already.caught -> InvalidCatchBlockSequence(553648315)

        }
    }
}
