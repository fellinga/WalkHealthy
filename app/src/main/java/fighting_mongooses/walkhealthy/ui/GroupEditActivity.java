package fighting_mongooses.walkhealthy.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.Group;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

public class GroupEditActivity extends AppCompatActivity {

    private Button btnSave;
    private EditText inputGroupname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.groupEditToolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getSupportActionBar().setTitle("Create new group");

        inputGroupname = (EditText) findViewById(R.id.groupname);
        btnSave = (Button) findViewById(R.id.btnSave);

        // Register Button Click event
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                createGroup();
            }
        });
    }

    /**
     * Takes the users input to create a new
     * group in the database.
     */
    private void createGroup() {
        String groupName = inputGroupname.getText().toString();
        Group grp = new Group(groupName, DatabaseTools.getCurrentFirebaseUser().getUid());
        if (DatabaseTools.createGroup(grp)) {
            Toast.makeText(GroupEditActivity.this, "Group created.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(GroupEditActivity.this, "Could not create group.", Toast.LENGTH_SHORT).show();
        }
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
