package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.UserHandle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.Event;
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
    private String groupName;
    private Toolbar toolbar;
    private TableLayout membersLayout, eventsLayout;
    private FloatingActionButton myFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (!getIntent().hasExtra(KEY_EXTRA)) {
            this.finish();
        }

        groupName = getIntent().getStringExtra(KEY_EXTRA);

        myFab = (FloatingActionButton) findViewById(R.id.addEvent);
        membersLayout = (TableLayout) findViewById(R.id.membersLayout);
        eventsLayout = (TableLayout) findViewById(R.id.eventsLayout);

        fetchGroupData();
    }

    /**
     * Reads the group object from the database
     * and fetches all group membe
     */
    private void fetchGroupData() {
        DatabaseTools.getDbGroupsReference().child(groupName).
            addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Group fbGroup = dataSnapshot.getValue(Group.class);
                    if (fbGroup != null) {
                        GroupActivity.this.group = fbGroup;
                        toolbar.setTitle(group.getName().toUpperCase());
                        toolbar.setSubtitle("Walk Healthy Event");
                        checkAdmin();
                        addMembers();
                        addEvents();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

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
    private void addMembers() {
        membersLayout.removeViews(0, membersLayout.getChildCount());
        for (Map.Entry<String, Boolean> entry : group.getMembers().entrySet()) {
            DatabaseTools.getDbUsersReference().child(entry.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            TableRow tr = new TableRow(GroupActivity.this);
                            TextView tv = new TextView(GroupActivity.this);
                            tv.setText("- " + user.getUsername());
                            tr.addView(tv);
                            membersLayout.addView(tr);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }
    }

    /**
     * Gets all group events and adds them
     * to the events lists.
     */
    private void addEvents() {
        eventsLayout.removeViews(0, eventsLayout.getChildCount());
        for (final Map.Entry<String, Boolean> entry : group.getEvents().entrySet()) {
            DatabaseTools.getDbEventsReference().child(entry.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Event event = dataSnapshot.getValue(Event.class);
                    if (event != null) {
                        TableRow tr = new TableRow(GroupActivity.this);
                        TextView tv = new TextView(GroupActivity.this);
                        tv.setText(event.getName());
                        tv.setTag(entry.getKey());
                        tv.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                final String eventId = view.getTag().toString();
                                openEventActivity(eventId);
                            }
                        });
                        tr.addView(tv);
                        eventsLayout.addView(tr);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Open the specified event activity and sends the
     * event id with it.
     *
     * @param eventId Id of the event
     */
    private void openEventActivity(String eventId) {
        // SEND THE GROUP NAME TO THE GROUP ACTIVITY
        Intent intent = new Intent(GroupActivity.this, EventActivity.class);
        intent.putExtra(EventActivity.KEY_EXTRA, eventId);
        startActivity(intent);
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
                DatabaseTools.createEvent(group.getName(), System.currentTimeMillis());
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
                return true;

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
