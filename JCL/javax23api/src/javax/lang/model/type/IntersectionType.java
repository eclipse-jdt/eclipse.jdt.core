package javax.lang.model.type;

import java.util.List;

public interface IntersectionType extends TypeMirror {

    List<? extends TypeMirror> getBounds();
}
