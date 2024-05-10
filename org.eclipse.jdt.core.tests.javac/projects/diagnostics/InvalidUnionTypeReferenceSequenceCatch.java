
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class InvalidUnionTypeReferenceSequenceCatch {
    public void testInvalidUnionTypeReferenceSequence() {
        try {
            boolean success = new File("f").createNewFile();
        } catch (FileNotFoundException | IOException e) {
            // compiler.err.multicatch.types.must.be.disjoint -> InvalidUnionTypeReferenceSequence(553649001)

        }
    }
}
