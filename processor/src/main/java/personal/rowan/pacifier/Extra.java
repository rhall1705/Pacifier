package personal.rowan.pacifier;

/**
 * Created by Rowan Hall
 */

public @interface Extra {

    String DEFAULT_PATH = "";

    String path() default DEFAULT_PATH;

    String[] paths() default {};
}
