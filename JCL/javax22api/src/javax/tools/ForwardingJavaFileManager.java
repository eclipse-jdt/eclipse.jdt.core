package javax.tools;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.JavaFileObject.Kind;

public class ForwardingJavaFileManager<M extends JavaFileManager> implements JavaFileManager {

    protected final M fileManager;

    protected ForwardingJavaFileManager(M fileManager) {
        this.fileManager = Objects.requireNonNull(fileManager);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return fileManager.getClassLoader(location);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location,
                                         String packageName,
                                         Set<Kind> kinds,
                                         boolean recurse)
        throws IOException
    {
        return fileManager.list(location, packageName, kinds, recurse);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        return fileManager.inferBinaryName(location, file);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return fileManager.isSameFile(a, b);
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        return fileManager.handleOption(current, remaining);
    }

    @Override
    public boolean hasLocation(Location location) {
        return fileManager.hasLocation(location);
    }

    @Override
    public int isSupportedOption(String option) {
        return fileManager.isSupportedOption(option);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location,
                                              String className,
                                              Kind kind)
        throws IOException
    {
        return fileManager.getJavaFileForInput(location, className, kind);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className,
                                               Kind kind,
                                               FileObject sibling)
        throws IOException
    {
        return fileManager.getJavaFileForOutput(location, className, kind, sibling);
    }

    @Override
    public JavaFileObject getJavaFileForOutputForOriginatingFiles(Location location,
                                               String className,
                                               Kind kind,
                                               FileObject... originatingFiles) throws IOException {
        try {
            Method delegate = getClass().getMethod("getJavaFileForOutput",
                                                   Location.class, String.class,
                                                   Kind.class, FileObject.class);
            if (delegate.getDeclaringClass() == ForwardingJavaFileManager.class) {
                return fileManager.getJavaFileForOutputForOriginatingFiles(location, className,
                                                                           kind, originatingFiles);
            } else {
                return JavaFileManager.super
                                      .getJavaFileForOutputForOriginatingFiles(location, className,
                                                                               kind, originatingFiles);
            }
        } catch (NoSuchMethodException ex) {
            throw new InternalError("This should never happen.", ex);
        }
    }

    @Override
    public FileObject getFileForInput(Location location,
                                      String packageName,
                                      String relativeName)
        throws IOException
    {
        return fileManager.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public FileObject getFileForOutput(Location location,
                                       String packageName,
                                       String relativeName,
                                       FileObject sibling)
        throws IOException
    {
        return fileManager.getFileForOutput(location, packageName, relativeName, sibling);
    }

    @Override
    public FileObject getFileForOutputForOriginatingFiles(Location location,
                                       String packageName,
                                       String relativeName,
                                       FileObject... originatingFiles) throws IOException {
        try {
            Method delegate = getClass().getMethod("getFileForOutput",
                                                   Location.class, String.class,
                                                   String.class, FileObject.class);
            if (delegate.getDeclaringClass() == ForwardingJavaFileManager.class) {
                return fileManager.getFileForOutputForOriginatingFiles(location, packageName,
                                                                       relativeName, originatingFiles);
            } else {
                return JavaFileManager.super
                                      .getFileForOutputForOriginatingFiles(location, packageName,
                                                                           relativeName, originatingFiles);
            }
        } catch (NoSuchMethodException ex) {
            throw new InternalError("This should never happen.", ex);
        }
    }

    @Override
    public void flush() throws IOException {
        fileManager.flush();
    }

    @Override
    public void close() throws IOException {
        fileManager.close();
    }

    @Override
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        return fileManager.getLocationForModule(location, moduleName);
    }

    @Override
    public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        return fileManager.getLocationForModule(location, fo);
    }

    @Override
    public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service) throws  IOException {
        return fileManager.getServiceLoader(location, service);
    }

    @Override
    public String inferModuleName(Location location) throws IOException {
        return fileManager.inferModuleName(location);
    }

    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        return fileManager.listLocationsForModules(location);
    }

    @Override
    public boolean contains(Location location, FileObject fo) throws IOException {
        return fileManager.contains(location, fo);
    }
}
