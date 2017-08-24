package personal.rowan.pacifier;

/**
 * Annotation to be used for member variables of a fragment class. Implement 'path' if a given
 * argument is only to be used in a particular entry point for the given fragment, but leave it
 * blank if the argument is required for all entry points. Use 'paths' if the argument is used
 * in multiple entry points, but not all of them.
 */
public @interface Argument {

    String DEFAULT_PATH = "";

    String[] value() default {DEFAULT_PATH};
}
