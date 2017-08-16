package personal.rowan.pacifier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        final EditText etString = (EditText) findViewById(R.id.et_string_extra);
        final EditText etInteger = (EditText) findViewById(R.id.et_int_extra);
        Button btnGo = (Button) findViewById(R.id.btn_go);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(
                        SecondActivityExtras.newIntent(FirstActivity.this,
                        etString.getText().toString(),
                        Integer.valueOf(etInteger.getText().toString())));
            }
        });
    }

}
