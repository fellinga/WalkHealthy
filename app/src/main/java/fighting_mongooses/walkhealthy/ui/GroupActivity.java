package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

/**
 * Group activity for group activities.
 *
 * This activity provides different actions for different groups.
 *
 * @author Mario Fellinger
 */
public class GroupActivity extends AppCompatActivity {

    public static final String KEY_EXTRA = "walkhealthy.KEY_GROUP";

    private Group group;
    private Toolbar groupToolbar;
    private TableLayout membersLayout, eventsLayout;
    private FloatingActionButton myFab;
    private List<User> groupMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(groupToolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        myFab = (FloatingActionButton) findViewById(R.id.addGrpAcc);
        membersLayout = (TableLayout) findViewById(R.id.membersLayout);
        eventsLayout = (TableLayout) findViewById(R.id.eventsLayout);

        if (getIntent().hasExtra(KEY_EXTRA)) {
            readGroupData();
        } else {
            this.finish();
        }
    }

    /**
     * Reads the group object from the database
     * and fetches all group membe
     */
    private void readGroupData() {
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
                checkAdmin();
                fetchMembers();
                fetchEvents();
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Adds the fabs button and its functionality.
     */
    private void checkAdmin() {
        if (isCurrentUserAdmin()) {
            myFab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openNewEventDialog();
                }
            });
            myFab.show();
        }
    }

    /**
     * Gets all group members and adds them
     * to the members lists.
     */
    private void fetchMembers() {
        membersLayout.removeViews(0, membersLayout.getChildCount());
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
                    membersLayout.addView(tr);
                }

                @Override
                public void onFailed(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Gets all group events and adds them
     * to the events lists.
     */
    private void fetchEvents() {
        eventsLayout.removeViews(0, eventsLayout.getChildCount());
        for (final Map.Entry<String, String> entry : group.getEvents().entrySet()) {
            TableRow tr = new TableRow(GroupActivity.this);
            TextView tv = new TextView(GroupActivity.this);
            tv.setText("Event: " + entry.getKey());
            if (isCurrentUserAdmin()) {
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        DatabaseTools.removeEvent(entry.getKey(), group.getName());
                        readGroupData();
                    }
                });
            }
            tr.addView(tv);
            eventsLayout.addView(tr);
        }
    }

    /**
     * Adds a new events
     */
    private void openNewEventDialog() {
        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(this);
        inputAlert.setTitle("Do you want to create a new event?");
        inputAlert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseTools.createEvent(group.getName());
                readGroupData();
                Toast.makeText(GroupActivity.this, "Event created.", Toast.LENGTH_SHORT).show();
            }
        });
        inputAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = inputAlert.create();
        alertDialog.show();
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

    /**
     * Checks if the current user is group admin.
     */
    private boolean isCurrentUserAdmin() {
        return DatabaseTools.getCurrentUsersUid().equals(group.getAdmin());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        readGroupData();
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

            case R.id.action_leavegrp:
                if (isCurrentUserAdmin()) {
                    Toast.makeText(GroupActivity.this, "You can not leave your own group.", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseTools.removeUserFromGroup(DatabaseTools.getCurrentUsersUid(), group.getName());
                    Toast.makeText(GroupActivity.this, "You left the group!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return true;

            case R.id.action_editgrp:
                if (isCurrentUserAdmin()) {
                    Intent intent = new Intent(GroupActivity.this, GroupEditActivity.class);
                    intent.putExtra(GroupEditActivity.KEY_EXTRA, group.getName());
                    startActivityForResult(intent, 0);
                } else {
                    Toast.makeText(GroupActivity.this, "Insufficient permissions.", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_deletegrp:
                if (isCurrentUserAdmin()) {
                    deleteGroup();
                } else {
                    Toast.makeText(GroupActivity.this, "Insufficient permissions.", Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
