package fighting_mongooses.walkhealthy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;
import fighting_mongooses.walkhealthy.utilities.VerificationTools;

/**
 * Register activity for user registration.
 *
 * This activity provides functions to create an users account.
 *
 * @author Mario Fellinger
 */
public class RegisterActivity extends Activity {

    // UI Elements
    private Button btnRegister, btnLinkToLogin;
    private EditText inputUsername, inputBirthday, inputEmail, inputPassword, inputConfirmPassword;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputUsername = (EditText) findViewById(R.id.name);
        inputBirthday = (EditText) findViewById(R.id.birthday);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String username = inputUsername.getText().toString().trim();
                String birthday = inputBirthday.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String passwordConfirm = inputConfirmPassword.getText().toString().trim();

                // VerifyCredentials() displays the appropriate error message if something is invalid.
                if (verifyCredentials(username, birthday, email, password, passwordConfirm)) {
                    createUser(email, birthday, password, username);
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openLoginActivity();
            }
        });

    }

    /**
     * Verifies a set of credentials using appropriate methods.
     * If there is an error, it will display the appropriate Toast notification.
     * @author Jake Gillenwater
     * @param username      The username to check against
     * @param birthday      A birthday to check against
     * @param email         An email to check against
     * @param password      The main password to check against
     * @param passwordConfirm   A secondary password to make sure the first was entered correctly
     * @return  True - if ALL parameters are proven valid, False - if ANY parameter is proven invalid
     */
    private boolean verifyCredentials(String username, String birthday, String email, String password, String passwordConfirm){
        // TODO: Move these literal strings into the "strings.xml" file, and then pull them from there.
        // Confirm each field
        // Return false with a toast notification upon failure to verify
        if(!VerificationTools.confirmUsername(username)){
            return returnWithToastPost(false, "Please enter a valid username.");
        }
        else if (!VerificationTools.confirmBirthday(birthday)){
            return returnWithToastPost(false, "Please enter a valid MM/DD/YYYY birthday.");
        }
        else if (!VerificationTools.confirmEmail(email)){
            return returnWithToastPost(false, "Please enter a valid Email address.");
        }
        else if (!password.equals(passwordConfirm)){
            return returnWithToastPost(false, "Passwords do not match.");
        }
        else if (!VerificationTools.confirmPassword(password)){
            return returnWithToastPost(false, "Please enter a valid password, " + VerificationTools.MIN_PASSWORD_LENGTH +
                                                             "-" + VerificationTools.MAX_PASSWORD_LENGTH + " characters long.");
        }
        // If it cannot be proven false, it must be true!
        return true;
    }



    /**
     * Displays a toast notification, then returns a copy of the given value.
     * @author Jake Gillenwater
     * @param value     The value to return
     * @param message   The message to display inside of a toast notification
     * @return          A copy of "value"
     */
    private boolean returnWithToastPost(boolean value, String message){
        Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG)
                .show();
        return value;
    }

    /**
     * Creates a new user via firebase authentication
     * takes the automated generated ID and creates the
     * user also in the firebase database where additional
     * information is stored (like birthday etc.).
     *
     * @author Mario Fellinger
     * @param email    The email address for the new user
     * @param birthday The birthday for the new user
     * @param password The password for the new user
     * @param username The username for the new user
     */
    private void createUser(final String email, final String birthday, final String password, final String username) {
        DatabaseTools.getFirebaseAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success - Firebase User created.
                            DatabaseTools.updateCurrentUser(new User(username, birthday));
                            DatabaseTools.addUserToGroup(DatabaseTools.getCurrentUsersUid(), DatabaseTools.ALL_USERS_GROUP);
                            DatabaseTools.getFirebaseAuth().getCurrentUser().sendEmailVerification();
                            openWelcomeActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * This method forwards the user to the
     * new user activity.
     */
    private void openLoginActivity() {
        startActivity(new Intent(this , LoginActivity.class));
        finish();
    }

    /**
     * This method forwards the user to the
     * new user activity.
     */
    private void openWelcomeActivity() {
        startActivity(new Intent(this , WelcomeActivity.class));
        finish();
    }

    /**
     * Handles the back button
     * on the device
     */
    @Override
    public void onBackPressed() {
        openLoginActivity();

        // Otherwise defer to system default behavior.
        super.onBackPressed();
    }

}
