package javax.annotation.processing;

import javax.tools.JavaFileManager;
import javax.tools.*;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.io.IOException;

public interface Filer {
    // Maintenance note: if the ability to create module-info files
    // through the Filer is added, add link to this method from
    // ModuleElement interface-level discussion.
    JavaFileObject createSourceFile(CharSequence name,
                                    Element... originatingElements) throws IOException;

    // Maintenance note: if the ability to create module-info files
    // through the Filer is added, add link to this method from
    // ModuleElement interface-level discussion.
    JavaFileObject createClassFile(CharSequence name,
                                   Element... originatingElements) throws IOException;

    FileObject createResource(JavaFileManager.Location location,
                             CharSequence moduleAndPkg,
                             CharSequence relativeName,
                             Element... originatingElements) throws IOException;

    FileObject getResource(JavaFileManager.Location location,
                           CharSequence moduleAndPkg,
                           CharSequence relativeName) throws IOException;
}
