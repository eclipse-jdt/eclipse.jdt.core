package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Abstract base implementation of all classpath variable initializers.
 * Classpath variable initializers are used in conjunction with the
 * "org.eclipse.jdt.core.classpathVariableInitializer" extension point.
 * <p>
 * Clients should subclass this class to implement a specific classpath
 * variable initializer. The subclass must have a public 0-argument
 * constructor and a concrete implementation of <code>initialize</code>.
 * </p>
 * @since 2.0
 */
public abstract class ClasspathVariableInitializer {

    /**
     * Creates a new classpath variable initializer.
     */
    public ClasspathVariableInitializer() {
    }

    /**
     * Binds a value to the workspace classpath variable with the given name,
     * or fails silently if this cannot be done.
     *
     * @param variable the name of the workspace classpath variable
     *    that requires a binding
     * @see JavaCore#setClasspathVariable     
     */
    public abstract void initialize(String variable);
}
