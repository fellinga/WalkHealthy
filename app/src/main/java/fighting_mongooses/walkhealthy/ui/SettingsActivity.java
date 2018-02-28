package fighting_mongooses.walkhealthy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;
import fighting_mongooses.walkhealthy.listener.OnGetUserListener;

/**
 * Settings activity for user information
 *
 * This activity shows users specific information.
 *
 * @author Mario Fellinger
 */
public class SettingsActivity extends AppCompatActivity {

    private Button deleteAccount;
    private TextView birthdayView, usernameView, emailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        birthdayView = (TextView) findViewById(R.id.birthday);
        usernameView = (TextView) findViewById(R.id.username);
        emailView = (TextView) findViewById(R.id.email);
        deleteAccount = (Button) findViewById(R.id.deleteAcc);

        deleteAccount.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                DatabaseTools.deleteUser();
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        updateInfo();
    }

    /**
     * Fetches the users information from the firebase
     * database and displays it.
     */
    private void updateInfo() {
        final FirebaseUser fbUser = DatabaseTools.getCurrentFirebaseUser();
        DatabaseTools.readUserData(fbUser.getUid(), new OnGetUserListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(User user) {
                birthdayView.setText(user.getBirthday());
                usernameView.setText(user.getUsername());
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });

        emailView.setText(fbUser.getEmail());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
