package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;

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
    private TableLayout memberlayout;
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

        memberlayout = (TableLayout) findViewById(R.id.memberlayout);

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
                    TableRow tr = new TableRow(GroupActivity.this);
                    TextView tv = new TextView(GroupActivity.this);
                    tv.setText("- " + user.getUsername());
                    tr.addView(tv);
                    memberlayout.addView(tr);
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

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return super.onOptionsItemSelected(item);

            case R.id.action_deletegrp:
                deleteGroup();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
