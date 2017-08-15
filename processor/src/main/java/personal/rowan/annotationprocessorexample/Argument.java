package personal.rowan.annotationprocessorexample;

public @interface Argument {

    String DEFAULT_PATH = "";

    String path() default DEFAULT_PATH;
}
