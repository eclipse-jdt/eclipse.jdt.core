package javax.annotation.processing;

import java.util.Map;
import java.util.Locale;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public interface ProcessingEnvironment {
    Map<String,String> getOptions();

    Messager getMessager();

    Filer getFiler();

    Elements getElementUtils();

    Types getTypeUtils();

    SourceVersion getSourceVersion();

    Locale getLocale();

    default boolean isPreviewEnabled() {
        return false;
    }
}
