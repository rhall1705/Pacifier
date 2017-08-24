package personal.rowan.pacifier;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pacifier.Pacifier;

/**
 * Created by Rowan Hall
 */

public class FirstFragment extends Fragment {

    @Argument
    String string;

    @Argument
    int integer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pacifier.bind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_first, viewGroup, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView tvString = (TextView) view.findViewById(R.id.tv_string_arg);
        TextView tvInteger = (TextView) view.findViewById(R.id.tv_int_arg);

        tvString.setText(string);
        tvInteger.setText(String.valueOf(integer));
    }

}
