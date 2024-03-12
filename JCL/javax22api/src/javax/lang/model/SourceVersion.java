package javax.lang.model;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public enum SourceVersion {
    RELEASE_0,

    RELEASE_1,
    RELEASE_2,
    RELEASE_3,
    RELEASE_4,
    RELEASE_5,
    RELEASE_6,
    RELEASE_7,
    RELEASE_8,
     RELEASE_9,
    RELEASE_10,
    RELEASE_11,
    RELEASE_12,
    RELEASE_13,
    RELEASE_14,
    RELEASE_15,
    RELEASE_16,
    RELEASE_17,
    RELEASE_18,
    RELEASE_19,
    RELEASE_20;

    public static SourceVersion latest() {
        return RELEASE_20;
    }

    private static final SourceVersion latestSupported = getLatestSupported();
    private static SourceVersion getLatestSupported() {
        int intVersion = Runtime.version().feature();
        return (intVersion >= 11) ?
            valueOf("RELEASE_" + Math.min(20, intVersion)):
            RELEASE_10;
    }

    public static SourceVersion latestSupported() {
        return latestSupported;
    }

    public static boolean isIdentifier(CharSequence name) {
        String id = name.toString();

        if (id.length() == 0) {
            return false;
        }
        int cp = id.codePointAt(0);
        if (!Character.isJavaIdentifierStart(cp)) {
            return false;
        }
        for (int i = Character.charCount(cp);
                i < id.length();
                i += Character.charCount(cp)) {
            cp = id.codePointAt(i);
            if (!Character.isJavaIdentifierPart(cp)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isName(CharSequence name) {
        return isName(name, latest());
    }

    public static boolean isName(CharSequence name, SourceVersion version) {
        String id = name.toString();

        for(String s : id.split("\\.", -1)) {
            if (!isIdentifier(s) || isKeyword(s, version))
                return false;
        }
        return true;
    }

    public static boolean isKeyword(CharSequence s) {
        return isKeyword(s, latest());
    }

    public static boolean isKeyword(CharSequence s, SourceVersion version) {
        String id = s.toString();
        switch(id) {
            // A trip through history
        case "strictfp":
            return version.compareTo(RELEASE_2) >= 0;

        case "assert":
            return version.compareTo(RELEASE_4) >= 0;

        case "enum":
            return version.compareTo(RELEASE_5) >= 0;

        case "_":
            return version.compareTo(RELEASE_9) >= 0;

     // case "non-sealed": can be added once it is a keyword only
     // dependent on release and not also preview features being
     // enabled.

            // Keywords common across versions

            // Modifiers
        case "public":    case "protected": case "private":
        case "abstract":  case "static":    case "final":
        case "transient": case "volatile":  case "synchronized":
        case "native":

            // Declarations
        case "class":     case "interface": case "extends":
        case "package":   case "throws":    case "implements":

            // Primitive types and void
        case "boolean":   case "byte":      case "char":
        case "short":     case "int":       case "long":
        case "float":     case "double":
        case "void":

            // Control flow
        case "if":      case "else":
        case "try":     case "catch":    case "finally":
        case "do":      case "while":
        case "for":     case "continue":
        case "switch":  case "case":     case "default":
        case "break":   case "throw":    case "return":

            // Other keywords
        case  "this":   case "new":      case "super":
        case "import":  case "instanceof":

            // Forbidden!
        case "goto":        case "const":

            // literals
        case "null":         case "true":       case "false":
            return true;

        default:
            return false;
        }
    }

    public static SourceVersion valueOf(Runtime.Version rv) {
        // Could also implement this as a switch where a case was
        // added with each new release.
        return valueOf("RELEASE_" + rv.feature());
    }

    public Runtime.Version runtimeVersion() {
        // The javax.lang.model API was added in JDK 6; for now,
        // limiting supported range to 6 and up.
        if (this.compareTo(RELEASE_6) >= 0) {
            return Runtime.Version.parse(Integer.toString(ordinal()));
        } else {
            return null;
        }
    }
}
