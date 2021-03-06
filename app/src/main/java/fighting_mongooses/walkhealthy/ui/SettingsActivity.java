package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.text.InputType;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;
import fighting_mongooses.walkhealthy.utilities.VerificationTools;

/**
 * Settings activity for user information
 *
 * This activity shows users specific information.
 *
 * @author Mario Fellinger
 */
public class SettingsActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;

    private Button deleteAccount, resetPassword;
    private TextView birthdayView, usernameView, emailView;
    private ImageView profilePic;
    private FloatingActionButton fab;
    private User user;

    private String newBirthdayText = "";
    private String newPasswordText = "";
    private String newUsernameText = "";
    private String newEmailText = "";

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
        resetPassword = (Button) findViewById(R.id.resetPassword);
        profilePic = (ImageView) findViewById(R.id.profilePic);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openFilePicker();
            }
        });
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                deleteAccount();
            }
        });
        resetPassword.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                changePassword();
            }
        });

        updateInfo();
        getProfilePicture();
    }

    /**
     * Fetches the users information from the firebase
     * database and displays it.
     */
    private void updateInfo() {
        DatabaseTools.getDbUsersReference().child(DatabaseTools.getCurrentUsersUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    SettingsActivity.this.user = user;
                    birthdayView.setText(user.getBirthday());
                    usernameView.setText(user.getUsername());
                    emailView.setText(DatabaseTools.getCurrentUsersEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getProfilePicture() {
        try {
            final File localFile = File.createTempFile("images", "jpg");
            DatabaseTools.getProfilePicture(DatabaseTools.getCurrentUsersUid(), localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            profilePic.setImageBitmap(myBitmap);
                            profilePic.setScaleType(ImageView.ScaleType.FIT_XY);
                            profilePic.requestLayout();
                        }
                    });
        } catch (Exception e) {
            // File not created
            Toast.makeText(this, "Profile pic not found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Opens a file picker dialog so that
     * the user can pick a profile picture.
     * This intent returns in onActivityResult()
     * where the picture gets finally set.
     */
    private void openFilePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    /**
     * Displays a alert box to confirm that
     * the user wants to delete the account.
     */
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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && data != null) {
                DatabaseTools.setProfilePicture(data.getData())
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            getProfilePicture();
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

//region CHANGE BIRTHDAY

    /**
     * Asks the user to enter a new birthday. Creates a pop-up.
     *
     * @author Jake Gillenwater
     */
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


    /**
     * Actually handles the updating the users birthday to the local user object and the database
     * @author Jake Gillenwater
     */
    private void onChangeBirthday(){

        // Format the birthday to match what the confirmBirthday() is expecting
        StringBuilder sb = new StringBuilder(newBirthdayText);
        sb.insert(2, "/");
        sb.insert(5, "/");
        newBirthdayText = sb.toString();

        // Verify the birth date
        if(VerificationTools.confirmBirthday(newBirthdayText)){
            // Update the database
            this.user.setBirthday(newBirthdayText);
            DatabaseTools.updateCurrentUser(this.user);
            // Reload info
            updateInfo();
        }
        else{
            // TODO: Change this to a proper Strings value message
            Toast.makeText(this, "Please enter a valid birthday - MMDDYYYY-",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //endregion

//region CHANGE PASSWORD

    /**
     * Ask the user to create a new password, and confirms it.
     * If verified, @see confirmPasswordReset().
     * If not verified, will display a toast notification.
     * @author Jake Gillenwater
     */
    private void changePassword(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Password:");
        // TODO: Set a message showing the requirements for a password

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newPasswordText = input.getText().toString();
                if(VerificationTools.confirmPassword(newPasswordText)){
                    confirmPasswordReset();
                }
                else{
                    // TODO: Change this to a proper Strings value message
                    Toast.makeText(getApplicationContext(), "Please enter a valid password.", Toast.LENGTH_SHORT).show();
                }
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

    /**
     * Ask the user to re-enter their password.
     * This is make sure the password does not contain a typo.
     * If the passwords match, @see onPasswordReset
     * @author Jake Gillenwater
     */
    private void confirmPasswordReset(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm New Password:");
        // TODO: Set a message explaining to re-enter the password

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input.getText().toString().equals(newPasswordText)){
                    reauthenticatePasswordChange();
                }
                else{
                    // TODO: Change this to a proper Strings value message
                    Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
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

    private void reauthenticatePasswordChange(){
        // Update database
        final FirebaseUser fbUser = DatabaseTools.getFirebaseAuth().getCurrentUser();
        final String currentEmail = DatabaseTools.getCurrentUsersEmail();

        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(SettingsActivity.this);
        inputAlert.setTitle("Re authenticate");
        final EditText passwordInput = new EditText(SettingsActivity.this);
        passwordInput.setHint("Your current password");
        inputAlert.setView(passwordInput);
        inputAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(currentEmail, passwordInput.getText().toString());

                // TODO: Error handling if the reauthenticate() or updatePassword() fails
                fbUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                fbUser.updatePassword(newPasswordText);
                                Toast.makeText(getApplicationContext(), "Password changed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        inputAlert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = inputAlert.create();
        alertDialog.show();
    }

//endregion

//region CHANGE USERNAME

    /**
     * Asks the user to enter a new username. Creates a pop-up.
     *
     * @author Jake Gillenwater
     * @param v     The view context of the text field clicked
     */
    public void onClickUsername(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Username:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newUsernameText = input.getText().toString();
                onChangeUsername();
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

    /**
     * Actually handles the updating the users username to the database
     * @author Jake Gillenwater
     */
    private void onChangeUsername(){
        if(VerificationTools.confirmUsername(newUsernameText)){
            // Update in Database
            this.user.setUsername(newUsernameText);
            DatabaseTools.updateCurrentUser(this.user);
            // Reload info
            updateInfo();
            Toast.makeText(getApplicationContext(), "Username changed.", Toast.LENGTH_SHORT).show();
        }
        else{
            // TODO: Change this to a proper Strings value message
            Toast.makeText(this, "Please enter a valid username",
                    Toast.LENGTH_SHORT).show();
        }

    }

//endregion

//region CHANGE EMAIL

    /**
     * Asks the user to enter a new email. Creates a pop-up.
     *
     * @author Jake Gillenwater
     * @param v     The view context of the text field clicked
     */
    public void onClickEmail(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Email:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newEmailText = input.getText().toString();
                onChangeEmail();
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

    /**
     * Actually handles the updating the users Email to the database
     * TODO: Send a new verification email to the New Email address. If the new Email is not confirmed within a certain time, it will keep the old Email.
     * @author Jake Gillenwater
     */
    private void onChangeEmail(){
        if(VerificationTools.confirmEmail(newEmailText)){
            // Update database
            final FirebaseUser fbUser = DatabaseTools.getFirebaseAuth().getCurrentUser();
            final String currentEmail = DatabaseTools.getCurrentUsersEmail();

            final AlertDialog.Builder inputAlert = new AlertDialog.Builder(this);
            inputAlert.setTitle("Re authenticate");
            final EditText passwordInput = new EditText(this);
            passwordInput.setHint("Your current password");
            inputAlert.setView(passwordInput);
            inputAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(currentEmail, passwordInput.getText().toString());

                    fbUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    fbUser.updateEmail(newEmailText)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    updateInfo();
                                                    Toast.makeText(getApplicationContext(), "Email changed.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                }
            });
            inputAlert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = inputAlert.create();
            alertDialog.show();
        }
        else{
            // TODO: Change this to a proper Strings value message
            Toast.makeText(this, "Please enter a valid Email",
                    Toast.LENGTH_SHORT).show();
        }

    }

//endregion

}
