package javax.tools;

import javax.tools.JavaFileManager.Location;

import java.util.concurrent.*;

public enum StandardLocation implements Location {

    CLASS_OUTPUT,

    SOURCE_OUTPUT,

    CLASS_PATH,

    SOURCE_PATH,

    ANNOTATION_PROCESSOR_PATH,

    ANNOTATION_PROCESSOR_MODULE_PATH,

    PLATFORM_CLASS_PATH,

    NATIVE_HEADER_OUTPUT,

    MODULE_SOURCE_PATH,

    UPGRADE_MODULE_PATH,

    SYSTEM_MODULES,

    MODULE_PATH,

    PATCH_MODULE_PATH;

    public static Location locationFor(final String name) {
        if (locations.isEmpty()) {
            // can't use valueOf which throws IllegalArgumentException
            for (Location location : values())
                locations.putIfAbsent(location.getName(), location);
        }
        name.getClass(); /* null-check */
        locations.putIfAbsent(name, new Location() {
                @Override
                public String getName() { return name; }
                @Override
                public boolean isOutputLocation() { return name.endsWith("_OUTPUT"); }
            });
        return locations.get(name);
    }
    //where
        private static final ConcurrentMap<String,Location> locations
            = new ConcurrentHashMap<>();

    @Override
    public String getName() { return name(); }

    @Override
    public boolean isOutputLocation() {
        switch (this) {
            case CLASS_OUTPUT:
            case SOURCE_OUTPUT:
            case NATIVE_HEADER_OUTPUT:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isModuleOrientedLocation() {
        switch (this) {
            case MODULE_SOURCE_PATH:
            case ANNOTATION_PROCESSOR_MODULE_PATH:
            case UPGRADE_MODULE_PATH:
            case SYSTEM_MODULES:
            case MODULE_PATH:
            case PATCH_MODULE_PATH:
                return true;
            default:
                return false;
        }
    }
}
