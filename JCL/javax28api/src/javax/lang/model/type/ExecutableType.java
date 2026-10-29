package javax.lang.model.type;


import java.util.List;

import javax.lang.model.element.ExecutableElement;

public interface ExecutableType extends TypeMirror {

    List<? extends TypeVariable> getTypeVariables();

    TypeMirror getReturnType();

    List<? extends TypeMirror> getParameterTypes();

    TypeMirror getReceiverType();

    List<? extends TypeMirror> getThrownTypes();
}
