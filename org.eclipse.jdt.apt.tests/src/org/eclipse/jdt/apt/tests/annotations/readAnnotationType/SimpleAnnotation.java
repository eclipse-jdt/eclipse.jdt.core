package org.eclipse.jdt.apt.tests.annotations.readAnnotationType;

public @interface SimpleAnnotation {
    
    enum Name
    {
        HELLO, GOOD_BYE
    }
    
    public Name value();
}
