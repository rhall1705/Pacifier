package personal.rowan.annotationprocessorexample;

public @interface Argument {

    public static final int DEFAULT_PATH = -1;

    int path() default DEFAULT_PATH;
}
