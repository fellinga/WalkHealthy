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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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

    private Button btnRegister, btnLinkToLogin;
    private EditText inputUsername, inputBirthday, inputEmail, inputPassword;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        inputUsername = (EditText) findViewById(R.id.name);
        inputBirthday = (EditText) findViewById(R.id.birthday);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String username = inputUsername.getText().toString().trim();
                String birthday = inputBirthday.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!username.isEmpty() && birthday.length() == 8 && !email.isEmpty() && !password.isEmpty()) {
                    createUser(email, birthday, password, username);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
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
     * Creates a new user via firebase authentication
     * takes the automated generated ID and creates the
     * user also in the firebase database where additional
     * information is stored (like birthday etc.).
     *
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
