package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.listener.OnGetGroupListener;
import fighting_mongooses.walkhealthy.listener.OnGetUserListener;
import fighting_mongooses.walkhealthy.objects.Group;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

public class GroupActivity extends AppCompatActivity {

    public static final String KEY_EXTRA = "walkhealthy.KEY_GROUP";

    private Group group;
    private Toolbar groupToolbar;
    private TextView adminName;
    private List<User> groupMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupToolbar = (Toolbar) findViewById(R.id.groupToolbar);
        setSupportActionBar(groupToolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        adminName = (TextView) findViewById(R.id.adminName);
        adminName.setText("Group member(s):");

        Button btnDeleteGrp = (Button) findViewById(R.id.deleteGrp);
        btnDeleteGrp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteGroup();
            }
        });

        if (getIntent().hasExtra(KEY_EXTRA)) {

            DatabaseTools.readGroupData(getIntent().getStringExtra(KEY_EXTRA), new OnGetGroupListener() {
                @Override
                public void onStart() {
                    // TODO BLOCK GUI WHILE GRP OBJECT IS LOADING
                }

                @Override
                public void onSuccess(Group group) {
                    GroupActivity.this.group = group;
                    groupToolbar.setTitle(group.getName().toUpperCase());
                    groupToolbar.setSubtitle("Walk Healthy Group");
                    fetchUsers();
                }

                @Override
                public void onFailed(DatabaseError databaseError) {

                }
            });
        } else {
            throw new IllegalArgumentException("Activity cannot find  extras " + KEY_EXTRA);
        }
    }

    /**
     * Gets all group members and adds them
     * to the users lists.
     */
    private void fetchUsers() {
        for (Map.Entry<String, String> entry : group.getMembers().entrySet()) {
            DatabaseTools.readUserData(entry.getKey(), new OnGetUserListener() {
                @Override
                public void onStart() {
                    // TODO BLOCK GUI WHILE GRP OBJECT IS LOADING
                }

                @Override
                public void onSuccess(User user) {
                    GroupActivity.this.groupMembers.add(user);
                    adminName.setText(adminName.getText() + "\n" + user.getUsername());
                }

                @Override
                public void onFailed(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Deletes the specified group and closes
     * the activity.
     */
    private void deleteGroup() {
        new AlertDialog.Builder(GroupActivity.this)
                .setTitle("Delete group.")
                .setMessage("Do you really want to delete " + group.getName() + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (DatabaseTools.removeGroup(group.getName())) {
                            Toast.makeText(GroupActivity.this, "Group deleted.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(GroupActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(GroupActivity.this, "Could not delete group.", Toast.LENGTH_SHORT).show();
                        }
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
}
