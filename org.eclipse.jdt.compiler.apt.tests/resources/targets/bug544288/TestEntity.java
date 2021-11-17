package targets.bug544288;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.MODULE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Target;


@Deprecated
public class TestEntity {

    @ATypeUse
    TestEmbeddable fieldWithTypeUse;
          
    @Target({ FIELD, TYPE })
    public @interface AType {
    }

    @Target({ FIELD })
    public @interface AField {
    }

    @Target({ FIELD, METHOD })
    public @interface AMethod {
    }

    @Target({ FIELD, PARAMETER })
    public @interface AParameter {
    }

    @Target({ FIELD, CONSTRUCTOR })
    public @interface AConstructor {
    }

    @Target({ FIELD, LOCAL_VARIABLE })
    public @interface ALocalVariable {
    }

    @Target({ FIELD, ANNOTATION_TYPE })
    public @interface AAnnotationType {
    }

    @Target({ FIELD, PACKAGE })
    public @interface APackage {
    }

    @Target({ FIELD, TYPE_PARAMETER })
    public @interface ATypeParameter {  
    }

    @Target({ FIELD, TYPE_USE })
    public @interface ATypeUse {
    }

    @Target({ FIELD, MODULE })
    public @interface AModule {
    }
}