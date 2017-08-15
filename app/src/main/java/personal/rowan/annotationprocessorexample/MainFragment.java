package personal.rowan.annotationprocessorexample;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class MainFragment extends Fragment {

    @Argument
    String stringArgument;

    @Argument
    double doubleArgument;

    @Argument(path="SecondPath")
    int integerArgument;

    @Argument(paths={"SecondPath", "ThirdPath"})
    int anotherIntArgument;

    @Argument(path="ThirdPath")
    String anotherStringArgument;

    @Argument(path="FourthPath")
    double anotherDoubleArgument;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainFragmentArguments.bind(this);
    }

}
