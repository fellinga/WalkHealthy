package fighting_mongooses.walkhealthy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import fighting_mongooses.walkhealthy.R;

/**
 * New user activity.
 *
 * This activity provides new user with valuable information.
 *
 * @author Mario Fellinger
 */
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

    /**
     * This method forwards the user to the
     * main activity.
     */
    private void openMainActivity() {
        startActivity(new Intent(this , MainActivity.class));
        finish();
    }

}
