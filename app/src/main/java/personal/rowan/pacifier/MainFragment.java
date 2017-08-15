package personal.rowan.pacifier;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import personal.rowan.pacifier.object.SomeObject;

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
    SomeObject objectArgument;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainFragmentArguments.bind(this);
    }

}
