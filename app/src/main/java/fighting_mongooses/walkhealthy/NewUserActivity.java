package fighting_mongooses.walkhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class NewUserActivity extends AppCompatActivity {

    private Button btnSkip, btnNewUser;
    private EditText ageField;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // Register Button Click event
        btnSkip = (Button) findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openMainActivity();
            }
        });

        ageField = (EditText) findViewById(R.id.age);

        btnNewUser = (Button) findViewById(R.id.btnNewUser);
        btnNewUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                addAgeToDatabase();
                openMainActivity();
            }
        });

    }

    private void addAgeToDatabase() {
        String age = ageField.getText().toString();
        if (age.equals("")) return;

        int ageInt = Integer.valueOf(age);

        DatabaseReference dataRef = mDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        dataRef.child("users").child(user.getUid()).child("age").setValue(ageInt);
    }

    private void openMainActivity() {
        startActivity(new Intent(this , MainActivity.class));
        finish();
    }

}
