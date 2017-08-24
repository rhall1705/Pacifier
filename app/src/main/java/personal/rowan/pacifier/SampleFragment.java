package personal.rowan.pacifier;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import pacifier.Pacifier;
import personal.rowan.pacifier.object.SomeObject;

/**
 * This fragment_first is never used, but serves to illustrate most of the potential use cases for
 * different @Argument types and paths.
 */
public class SampleFragment extends Fragment {

    private static final String SECOND_PATH = "SecondPath";
    private static final String THIRD_PATH = "ThirdPath";
    private static final String FOURTH_PATH = "FourthPath";

    @Argument
    String stringArgument;

    @Argument
    double doubleArgument;

    @Argument(SECOND_PATH)
    int integerArgument;

    @Argument({SECOND_PATH, THIRD_PATH})
    int anotherIntArgument;

    @Argument(THIRD_PATH)
    String anotherStringArgument = "defaultValue";

    @Argument(FOURTH_PATH)
    SomeObject objectArgument;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pacifier.bind(this);
    }

}
