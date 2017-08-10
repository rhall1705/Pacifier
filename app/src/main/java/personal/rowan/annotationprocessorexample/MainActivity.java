package personal.rowan.annotationprocessorexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Extra
    String mainActivityArgument;

    @Extra
    int someIntegerExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityExtras.bind(this);
    }
}
