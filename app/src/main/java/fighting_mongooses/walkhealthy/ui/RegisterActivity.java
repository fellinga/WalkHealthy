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
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

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

    // Firebase
    private FirebaseAuth mAuth;

    // Data Validation Constants
    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int MAX_USERNAME_LENGTH = 128;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 64;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

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
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
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
        if(!confirmUsername(username)){
            return returnWithToastPost(false, "Please enter a valid username.");
        }
        else if (!confirmBirthday(birthday)){
            return returnWithToastPost(false, "Please enter a valid MM/DD/YYYY birthday.");
        }
        else if (!confirmEmail(email)){
            return returnWithToastPost(false, "Please enter a valid Email address.");
        }
        else if (!password.equals(passwordConfirm)){
            return returnWithToastPost(false, "Passwords do not match.");
        }
        else if (!confirmPassword(password)){
            return returnWithToastPost(false, "Please enter a valid password, 8-63 characters long.");
        }
        // If it cannot be proven false, it must be true!
        return true;
    }

    /**
     * Verifies the input of a Birthday. Checks if it satisfies a simple REGEX of ##/##/####
     * @author Jake Gillenwater
     * @param birthday The date string to check against
     * @return  True - if valid, False - if there is a problem with it.
     */
    private boolean confirmBirthday(String birthday){
        // TODO: Verify based on age as well. No one over 100, under TBT, or from the future
        // TODO: Verify this is an actual date (i.e. no 67th month, no June 34th)
        Pattern p = Pattern.compile("[\\d]{1,2}/[\\d]{1,2}/[\\d]{4}");
        Matcher m = p.matcher(birthday);
        return m.matches();
    }

    /**
     * Verifies the input of an Email address. Checks if satisfies a simple REGEX.
     * @author Jake Gillenwater
     * @param email     The Email to check against
     * @return      True - if valid, False - if there is a problem with it.
     */
    private boolean confirmEmail(String email){
        Pattern p = Pattern.compile("[a-zA-Z0123456789\\_\\-\\!\\#\\$\\%\\&]{2,128}@[[a-zA-Z0123456789\\_\\-\\!\\#\\$\\%\\&]\\{2, 128\\}\\}\\.]+[a-zA-Z0123456789\\_\\-\\!\\#\\$\\%\\&]\\{2,8\\}");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * Verifies the input of a password. Checks if is a proper length.
     * @author Jake Gillenwater
     * @param password The password to check against
     * @return  True - if valid, False- if there is a problem with it.
     */
    private boolean confirmPassword(String password){
        return (password.length() > MIN_PASSWORD_LENGTH && password.length() <= MAX_PASSWORD_LENGTH);
    }

    /**
     * Verifies the input of a user name. Checks if contains a value, and has a proper length.
     * @author Jake Gillenwater
     * @param username  The username to check against
     * @return  True - if valid, False - if there is a problem with it.
     */
    private boolean confirmUsername(String username){
        return (!username.isEmpty() && username.length() > MIN_USERNAME_LENGTH && username.length() <= MAX_USERNAME_LENGTH);
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
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            User user = new User(username, birthday);
                            DatabaseTools.createUser(user);

                            openNewUserActivity();
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
    private void openNewUserActivity() {
        startActivity(new Intent(this , NewUserActivity.class));
        finish();
    }

}
