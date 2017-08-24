package personal.rowan.pacifier;

/**
 * Created by Rowan Hall
 */

public @interface Extra {

    String DEFAULT_PATH = "";

    String[] value() default {DEFAULT_PATH};
}
