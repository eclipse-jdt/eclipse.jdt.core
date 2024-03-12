package javax.lang.model.type;


public interface ArrayType extends ReferenceType {

    TypeMirror getComponentType();
}
