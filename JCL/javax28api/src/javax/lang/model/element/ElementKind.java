package javax.lang.model.element;

public enum ElementKind {

    /** A package. */
    PACKAGE,

    // Declared types
    /** An enum class. */
    ENUM,
    /**
     * A class not described by a more specific kind (like {@code
     * ENUM} or {@code RECORD}).
     */
    CLASS,

    /** An annotation interface. (Formerly known as an annotation type.) */
    ANNOTATION_TYPE,
    /**
     * An interface not described by a more specific kind (like
     * {@code ANNOTATION_TYPE}).
     */
    INTERFACE,

    // Variables
    /** An enum constant. */
    ENUM_CONSTANT,
    /**
     * A field not described by a more specific kind (like
     * {@code ENUM_CONSTANT}).
     */
    FIELD,
    /** A parameter of a method or constructor. */
    PARAMETER,
    /** A local variable. */
    LOCAL_VARIABLE,
    /** A parameter of an exception handler. */
    EXCEPTION_PARAMETER,

    // Executables
    /** A method. */
    METHOD,
    /** A constructor. */
    CONSTRUCTOR,
    /** A static initializer. */
    STATIC_INIT,
    /** An instance initializer. */
    INSTANCE_INIT,

    /** A type parameter. */
    TYPE_PARAMETER,

    /**
     * An implementation-reserved element.  This is not the element
     * you are looking for.
     */
    OTHER,

    // Constants added since initial release

    /**
     * A resource variable.
     * @since 1.7
     */
     RESOURCE_VARIABLE,

    /**
     * A module.
     * @since 9
     */
     MODULE,

    /**
     * A record class.
     * @since 16
     */
    RECORD,

    /**
     * A record component of a {@code record}.
     * @since 16
     */
    RECORD_COMPONENT,

    /**
     * A binding variable in a pattern.
     * @since 16
     */
    BINDING_VARIABLE;

    // Maintenance note: check if the default implementation of
    // Elements.getOutermostTypeElement needs updating when new kind
    // constants are added.

    public boolean isClass() {
        return this == CLASS || this == ENUM || this == RECORD;
    }

    public boolean isInterface() {
        return this == INTERFACE || this == ANNOTATION_TYPE;
    }

    public boolean isDeclaredType() {
        return isClass() || isInterface();
    }

    public boolean isField() {
        return this == FIELD || this == ENUM_CONSTANT;
    }

    public boolean isExecutable() {
        switch(this) {
        	case METHOD:
        	case CONSTRUCTOR:
        	case  STATIC_INIT:
        	case INSTANCE_INIT: 
        		return true;
		default:
			break;
        };
        return false;
    }

    public boolean isInitializer() {
    	
        switch(this) {
        case STATIC_INIT:
        case INSTANCE_INIT:
        	return true;
        default:
        	break;
        };
        return false;
    }
    public boolean isVariable() {
        switch(this) {
        case ENUM_CONSTANT:
        case FIELD:
        case PARAMETER:
        case LOCAL_VARIABLE:
        case EXCEPTION_PARAMETER:
        case RESOURCE_VARIABLE:
        case BINDING_VARIABLE:
        	return true;
		default:
			break;
        };
        return false;
    }
}
