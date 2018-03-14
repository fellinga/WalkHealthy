package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.text.InputType;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;
import fighting_mongooses.walkhealthy.listener.OnGetUserListener;
import fighting_mongooses.walkhealthy.utilities.VerificationTools;

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
    private User user;

    private String newBirthdayText = "";

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
                deleteAccount();
            }
        });

        updateInfo();
    }

    /**
     * Fetches the users information from the firebase
     * database and displays it.
     */
    private void updateInfo() {
        final FirebaseUser fbUser = DatabaseTools.getFirebaseAuth().getCurrentUser();
        DatabaseTools.readUserData(fbUser.getUid(), new OnGetUserListener() {
            @Override
            public void onStart() {
                // TODO BLOCK GUI WHILE USER OBJECT IS LOADING
            }

            @Override
            public void onSuccess(User user) {
                SettingsActivity.this.user = user;
                birthdayView.setText(user.getBirthday());
                usernameView.setText(user.getUsername());
                emailView.setText(fbUser.getEmail());
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }

    private void deleteAccount() {
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Delete Account.")
                .setMessage("Do you really want to delete your account?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseTools.deleteUser();
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


    public void onClickBirthday(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Birthday");
        builder.setMessage("Format: MMDDYYYY");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newBirthdayText = input.getText().toString();
                onChangeBirthday();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void onChangeBirthday(){

        // Format the birthday to match what the confirmBirthday() is expecting
        StringBuilder sb = new StringBuilder(newBirthdayText);
        sb.insert(2, "/");
        sb.insert(5, "/");
        newBirthdayText = sb.toString();

        // Verify the birthdate
        if(VerificationTools.confirmBirthday(newBirthdayText)){

            // If valid, update the local user object's birthday
            this.user.setBirthday(newBirthdayText);
            // Update the UI
            birthdayView.setText(newBirthdayText);
            // Update the database
            DatabaseTools.updateCurrentUser(this.user);

        }
        else{
            Toast.makeText(this, "Please enter a valid birthday - MMDDYYYY-"+newBirthdayText,
                    Toast.LENGTH_SHORT).show();
        }
    }

}
