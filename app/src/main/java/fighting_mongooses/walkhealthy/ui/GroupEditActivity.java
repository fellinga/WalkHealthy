package fighting_mongooses.walkhealthy.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private Group group;
    private EditText inputGroupname;
    private TableLayout alluserlayout, addeduserlayout, adminsLayout;
    private List<String> groupMember = new ArrayList<>();
    private List<String> removedUsers = new ArrayList<>();
    private List<String> groupAdmins = new ArrayList<>();
    private Button mainLocBtn;
    private Place mainPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        }

        inputGroupname = findViewById(R.id.groupname);
        alluserlayout = findViewById(R.id.alluserlayout);
        addeduserlayout = findViewById(R.id.addeduserlayout);
        adminsLayout = findViewById(R.id.adminsLayout);
        mainLocBtn = findViewById(R.id.mainLocBtn);

        if (getIntent().hasExtra(KEY_EXTRA)) {
            getSupportActionBar().setTitle("Edit group");
            inputGroupname.setBackgroundColor(Color.GRAY);
            inputGroupname.setFocusable(false);
            fetchGroupData(getIntent().getStringExtra(KEY_EXTRA));
        } else {
            getSupportActionBar().setTitle("New group");
            group = new Group();
            group.addAdmin(DatabaseTools.getCurrentUsersUid());
        }

        mainLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openMap(); }
        });

        fetchAllUsers();
    }

    /**
     * Load existing group members if
     * group gets edited (name change not
     * supported right now)
     */
    private void fetchGroupData(final String groupName) {
        // LOAD GROUP OBJECT
        DatabaseTools.getDbGroupsReference().child(groupName).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        group = dataSnapshot.getValue(Group.class);
                        if (group != null) {
                            inputGroupname.setText(group.getName());
                            addMembers();
                            addAdmins();
                        } else {
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Gets all group members and adds them
     * to the members lists.
     */
    private void addMembers() {
        for (final Map.Entry<String,Object> entry : group.getMembers().entrySet()) {
            DatabaseTools.getDbUsersReference().child(entry.getKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                addUserToInvitedUsers(entry.getKey(), user.getUsername());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    /**
     * Gets all group admins and adds them
     * to the admins lists.
     */
    private void addAdmins() {
        for (final Map.Entry<String,Object> entry : group.getAdmins().entrySet()) {
            DatabaseTools.getDbUsersReference().child(entry.getKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final User user = dataSnapshot.getValue(User.class);
                            if (user != null) {
                                addUserToGroupAdmins(entry.getKey(), user.getUsername());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    /**
     * Add an user from the invited member list
     *
     * @param uid      User ID number
     * @param username Users username
     */
    private void addUserToInvitedUsers(final String uid, final String username) {
        if (!groupMember.contains(uid)) {
            group.addMember(uid);
            groupMember.add(uid);
            TableRow tr = new TableRow(GroupEditActivity.this);
            TextView tv = new TextView(GroupEditActivity.this);
            tv.setText(username);
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(GroupEditActivity.this);
                    builder.setTitle("WARNING");
                    builder.setCancelable(true);
                    builder.setIcon(R.drawable.ic_edit_black_24dp);
                    builder.setMessage("Do you want to remove the user or add as admin?");
                    builder.setNegativeButton("REMOVE",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeUserFromInvitedUsers(uid, username);
                                }
                            });
                    builder.setPositiveButton("ADMIN",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    addUserToGroupAdmins(uid, username);
                                }
                            });
                    builder.show();
                }

            });
            tr.addView(tv);
            addeduserlayout.addView(tr);
        }
    }

    /**
     * Add an user to the admins list
     *
     * @param uid      User ID number
     * @param username Users username
     */
    private void addUserToGroupAdmins(final String uid, final String username) {
        if (!groupAdmins.contains(uid)) {
            group.addAdmin(uid);
            groupAdmins.add(uid);
            TableRow tr = new TableRow(GroupEditActivity.this);
            TextView tv = new TextView(GroupEditActivity.this);
            tv.setText(username);
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeUserFromAdmins(uid, username);
                }

            });
            tr.addView(tv);
            adminsLayout.addView(tr);
        }
    }

    /**
     * Removes an user from the invited member list
     *
     * @param uid      User ID number
     * @param username Users username
     */
    private void removeUserFromInvitedUsers(final String uid, final String username) {
        if (!DatabaseTools.getCurrentUsersUid().equals(uid)) {
            removedUsers.add(uid);
            for (int i = 0; i < addeduserlayout.getChildCount(); i++) {
                TableRow tr = (TableRow) addeduserlayout.getChildAt(i);
                TextView tv = (TextView) tr.getChildAt(0);
                if (tv.getText().toString().equals(username)) {
                    group.removeMember(uid);
                    groupMember.remove(uid);
                    addeduserlayout.removeViewAt(i);
                    removeUserFromAdmins(uid, username);
                }
            }
        }
    }

    /**
     * Removes an user from the admins
     *
     * @param uid      User ID number
     * @param username Users username
     */
    private void removeUserFromAdmins(final String uid, final String username) {
        if (!DatabaseTools.getCurrentUsersUid().equals(uid)) {
            for (int i = 0; i < adminsLayout.getChildCount(); i++) {
                TableRow tr = (TableRow) adminsLayout.getChildAt(i);
                TextView tv = (TextView) tr.getChildAt(0);
                if (tv.getText().toString().equals(username)) {
                    group.removeAdmin(uid);
                    groupAdmins.remove(uid);
                    adminsLayout.removeViewAt(i);
                }
            }
        }
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

                    // ADD CURRENT USER TO ADDED USER LIST
                    if (snapshot.getKey().equals(DatabaseTools.getCurrentUsersUid())) {
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
     * Takes the users input to create a new
     * group in the database.
     */
    private void createGroup() {
        String groupName = inputGroupname.getText().toString();

        if(groupName != null && !groupName.isEmpty() && mainPlace != null){
            // TODO: Verify this information as valid by adding some more functionality to the VerificationTools class
            group.setName(groupName);

            // Create location object
            DatabaseTools.getGeoFire().setLocation(groupName,
                    new GeoLocation(mainPlace.getLatLng().latitude, mainPlace.getLatLng().longitude),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            // listener needed
                        }
                    });
        }

        // remove all users from the database who got removed (only edit)
        for (String uid : removedUsers) {
            DatabaseTools.removeUserFromGroup(uid, groupName);
        }

        if (DatabaseTools.createGroup(group)) {
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
        getMenuInflater().inflate(R.menu.menu_edit_toolbar, menu);
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

    /**
     * Open map to pick the main group location
     */
    private void openMap() {
        int requestCode = 1222;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        if (mainPlace != null) {
            builder.setLatLngBounds(new LatLngBounds(mainPlace.getLatLng(), mainPlace.getLatLng()));
        }

        try {
            startActivityForResult(builder.build(this), requestCode);
        }
        catch (Exception e) {
            // TODO: Handle this exception
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Just double check that this was the correct return from setting the group main location.
        if (requestCode == 1222 && resultCode == RESULT_OK) {
           setMainPlace( PlacePicker.getPlace(this, data) );
        }
    }

    private void setMainPlace(Place p){
        mainPlace = p;
    }

}
