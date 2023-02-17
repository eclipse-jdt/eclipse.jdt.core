package javax.tools;

import javax.lang.model.element.NestingKind;
import javax.lang.model.element.Modifier;
import java.util.Objects;

public interface JavaFileObject extends FileObject {

    enum Kind {
        SOURCE(".java"),

        CLASS(".class"),

        HTML(".html"),

        OTHER("");
        public final String extension;
        Kind(String extension) {
            this.extension = Objects.requireNonNull(extension);
        }
    }

    Kind getKind();

    boolean isNameCompatible(String simpleName, Kind kind);

    NestingKind getNestingKind();

    Modifier getAccessLevel();

}
