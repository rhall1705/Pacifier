package personal.rowan.annotationprocessorexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

@CustomAnnotation
public class MainActivity extends AppCompatActivity {

    @CustomAnnotation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
