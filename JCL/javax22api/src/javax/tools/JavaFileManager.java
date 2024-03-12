package javax.tools;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;

import static javax.tools.JavaFileObject.Kind;

public interface JavaFileManager extends Closeable, Flushable, OptionChecker {

    interface Location {
        String getName();

        boolean isOutputLocation();

        default boolean isModuleOrientedLocation() {
            return getName().matches("\\bMODULE\\b");
        }
    }

    ClassLoader getClassLoader(Location location);

    Iterable<JavaFileObject> list(Location location,
                                  String packageName,
                                  Set<Kind> kinds,
                                  boolean recurse)
        throws IOException;

    String inferBinaryName(Location location, JavaFileObject file);

    boolean isSameFile(FileObject a, FileObject b);

    boolean handleOption(String current, Iterator<String> remaining);

    boolean hasLocation(Location location);

    JavaFileObject getJavaFileForInput(Location location,
                                       String className,
                                       Kind kind)
        throws IOException;

    JavaFileObject getJavaFileForOutput(Location location,
                                        String className,
                                        Kind kind,
                                        FileObject sibling)
        throws IOException;

    default JavaFileObject getJavaFileForOutputForOriginatingFiles(Location location,
                                        String className,
                                        Kind kind,
                                        FileObject... originatingFiles)
        throws IOException {
        return getJavaFileForOutput(location, className, kind, siblingFrom(originatingFiles));
    }

    FileObject getFileForInput(Location location,
                               String packageName,
                               String relativeName)
        throws IOException;

    FileObject getFileForOutput(Location location,
                                String packageName,
                                String relativeName,
                                FileObject sibling)
        throws IOException;

    default FileObject getFileForOutputForOriginatingFiles(Location location,
                                String packageName,
                                String relativeName,
                                FileObject... originatingFiles)
        throws IOException {
        return getFileForOutput(location, packageName, relativeName, siblingFrom(originatingFiles));
    }

    @Override
    void flush() throws IOException;

    @Override
    void close() throws IOException;

    // TODO: describe failure modes
    default Location getLocationForModule(Location location, String moduleName) throws IOException {
        throw new UnsupportedOperationException();
    }

    default Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        throw new UnsupportedOperationException();
    }

    // TODO: describe failure modes
    default <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service) throws  IOException {
        throw new UnsupportedOperationException();
    }

    // TODO: describe failure modes
    default String inferModuleName(Location location) throws IOException {
        throw new UnsupportedOperationException();
    }

    // TODO: describe failure modes
    default Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        throw new UnsupportedOperationException();
    }

    default boolean contains(Location location, FileObject fo) throws IOException {
        throw new UnsupportedOperationException();
    }

    private static FileObject siblingFrom(FileObject[] originatingFiles) {
        return originatingFiles != null && originatingFiles.length > 0 ? originatingFiles[0] : null;
    }

}
