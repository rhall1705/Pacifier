package personal.rowan.annotationprocessorexample;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class MainFragment extends Fragment {

    @Argument
    String someFragmentArg;

    @Argument
    double someDoubleArg;

    @Argument(path=1)
    int someInt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainFragmentArguments.bind(this);
    }

}
