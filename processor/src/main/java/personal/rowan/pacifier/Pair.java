package personal.rowan.pacifier;

/**
 * Created by Rowan Hall
 */

public class Pair<A, B> {

    private A first;
    private B second;

    private Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A first() {
        return first;
    }

    public B second() {
        return second;
    }

    public static <A, B> Pair<A, B> create(A first, B second) {
        return new Pair<>(first, second);
    }

}
