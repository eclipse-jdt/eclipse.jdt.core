package javax.tools;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class ToolProvider {

    private static final String systemJavaCompilerModule = "jdk.compiler";
    private static final String systemJavaCompilerName   = "com.sun.tools.javac.api.JavacTool";

    private ToolProvider() {}

    public static JavaCompiler getSystemJavaCompiler() {
        return getSystemTool(JavaCompiler.class,
                systemJavaCompilerModule, systemJavaCompilerName);
    }

    private static final String systemDocumentationToolModule = "jdk.javadoc";
    private static final String systemDocumentationToolName = "jdk.javadoc.internal.api.JavadocTool";

    public static DocumentationTool getSystemDocumentationTool() {
        return getSystemTool(DocumentationTool.class,
                systemDocumentationToolModule, systemDocumentationToolName);
    }

    @Deprecated(since="9")
    public static ClassLoader getSystemToolClassLoader() {
        return null;
    }

    private static <T> T getSystemTool(Class<T> clazz, String moduleName, String className) {

        try {
            ServiceLoader<T> sl = ServiceLoader.load(clazz, ClassLoader.getSystemClassLoader());
            for (T tool : sl) {
                if (matches(tool, moduleName))
                    return tool;
            }
        } catch (ServiceConfigurationError e) {
            throw new Error(e);
        }
        return null;
    }

    @SuppressWarnings("removal")
    private static <T> boolean matches(T tool, String moduleName) {
        PrivilegedAction<Boolean> pa = () -> {
            Module toolModule = tool.getClass().getModule();
            String toolModuleName = toolModule.getName();
            return Objects.equals(toolModuleName, moduleName);
        };
        return AccessController.doPrivileged(pa);
    }
}
