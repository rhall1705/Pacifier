package personal.rowan.pacifier;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pacifier.Pacifier;

/**
 * Created by Rowan Hall
 */

public class SecondActivity extends AppCompatActivity {

    @Extra
    String string;

    @Extra
    int integer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Pacifier.bind(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_fragment_container, FirstFragmentArguments.newInstance(string, integer))
                .commit();
    }

}
