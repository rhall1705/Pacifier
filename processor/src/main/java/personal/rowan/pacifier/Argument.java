package personal.rowan.pacifier;

public @interface Argument {

    String DEFAULT_PATH = "";

    String path() default DEFAULT_PATH;

    String[] paths() default {};
}
