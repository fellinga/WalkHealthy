package fighting_mongooses.walkhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class NewUserActivity extends AppCompatActivity {

    private Button btnSkip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);

        // Register Button Click event
        btnSkip = (Button) findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openMainActivity();
            }
        });
    }

    private void openMainActivity() {
        startActivity(new Intent(this , MainActivity.class));
        finish();
    }

}
