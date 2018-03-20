package fighting_mongooses.walkhealthy.ui;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.Group;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

/**
 * GroupEdit dialog for group activities.
 *
 * This activity provides lets you create or modify groups.
 *
 * @author Mario Fellinger
 */
public class GroupEditActivity extends AppCompatActivity {

    public static final String KEY_EXTRA = "walkhealthy.KEY_GROUPEDIT";

    private EditText inputGroupname;
    private TableLayout alluserlayout, addeduserlayout;
    private List<String> invitedUsers = new ArrayList<>();
    private List<String> removedUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        }

        inputGroupname = (EditText) findViewById(R.id.groupname);
        alluserlayout = (TableLayout) findViewById(R.id.alluserlayout);
        addeduserlayout = (TableLayout) findViewById(R.id.addeduserlayout);

        if (getIntent().hasExtra(KEY_EXTRA)) {
            getSupportActionBar().setTitle("Edit group");
            loadGrpData(getIntent().getStringExtra(KEY_EXTRA));
        } else {
            getSupportActionBar().setTitle("New group");
        }

        fetchAllUsers();
    }

    /**
     * Loads all users from the database
     */
    private void fetchAllUsers() {
        DatabaseTools.getDbUsersReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                alluserlayout.removeViewsInLayout(0, alluserlayout.getChildCount());
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final User user = snapshot.getValue(User.class);
                    if (snapshot.getKey().equals(DatabaseTools.getCurrentUsersUid())) {
                        addUserToInvitedUsers(DatabaseTools.getCurrentUsersUid(), user.getUsername());
                    }
                    TableRow tr = new TableRow(GroupEditActivity.this);
                    TextView tv = new TextView(GroupEditActivity.this);
                    tv.setText(user.getUsername());
                    tr.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addUserToInvitedUsers(snapshot.getKey(), user.getUsername());
                        }

                    });
                    tr.addView(tv);
                    alluserlayout.addView(tr);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Load existing group members if
     * group gets edited (name change not
     * supported right now)
     */
    private void loadGrpData(final String groupName) {
        inputGroupname.setText(groupName);
        inputGroupname.setBackgroundColor(Color.GRAY);
        inputGroupname.setFocusable(false);

        // LOAD GROUP MEMBER
        DatabaseTools.getDbUsersReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("groups").hasChild(groupName)) {
                        final User user = snapshot.getValue(User.class);
                        addUserToInvitedUsers(snapshot.getKey(), user.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Add an user from the invited member list
     *
     * @param uid      User ID number
     * @param username Users username
     */
    private void addUserToInvitedUsers(final String uid, final String username) {
        if (!invitedUsers.contains(uid)) {
            invitedUsers.add(uid);
            TableRow tr = new TableRow(GroupEditActivity.this);
            TextView tv = new TextView(GroupEditActivity.this);
            tv.setText(username);
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeUserFromInvitedUsers(uid, username);
                }

            });
            tr.addView(tv);
            addeduserlayout.addView(tr);
        }
    }

    /**
     * Removes an user from the invited member list
     *
     * @param uid      User ID number
     * @param username Users username
     */
    private boolean removeUserFromInvitedUsers(final String uid, final String username) {
        if (!DatabaseTools.getCurrentUsersUid().equals(uid)) {
            removedUsers.add(uid);
            for (int i = 0; i < addeduserlayout.getChildCount(); i++) {
                TableRow tr = (TableRow) addeduserlayout.getChildAt(i);
                TextView tv = (TextView) tr.getChildAt(0);
                if (tv.getText().toString().equals(username)) {
                    invitedUsers.remove(uid);
                    addeduserlayout.removeViewAt(i);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Takes the users input to create a new
     * group in the database.
     */
    private void createGroup() {
        String groupName = inputGroupname.getText().toString();
        Group grp = new Group(groupName, DatabaseTools.getCurrentUsersUid());

        // remove all users from the database who got removed (only edit)
        for (String uid : removedUsers) {
            DatabaseTools.removeUserFromGroup(uid, groupName);
        }

        // add all users to group
        for (String uid : invitedUsers) {
            grp.addMember(uid);
        }

        if (DatabaseTools.createGroup(grp)) {
            Toast.makeText(GroupEditActivity.this, "Success.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(GroupEditActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
        }
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_groupedit_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_save:
                createGroup();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
