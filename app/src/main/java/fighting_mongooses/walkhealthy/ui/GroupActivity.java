package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.adapter.ViewHolder;
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

        fetchGroupData();
        fetchMembers();
        fetchEvents();
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
                        groupName = group.getName();
                        toolbar.setTitle(groupName);
                        toolbar.setSubtitle("Walk Healthy Group");
                        handleFab();
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
    private void handleFab() {
        if (isCurrentUserAdmin()) {
            FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.addEvent);
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
        final RecyclerView memberRecyclerView = (RecyclerView)findViewById(R.id.memberRecyclerView);
        Query query = DatabaseTools.getDbUsersReference().orderByChild("groups/" + groupName).equalTo(true);

        final FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, ViewHolder> memberAdapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, User model) {
                holder.setTitle(model.getUsername());
                holder.setDescription(model.getBirthday());
                holder.setImageView(R.drawable.ic_account_circle_black_24dp);
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.view_row for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_row, parent, false);
                return new ViewHolder(view);
            }
        };

        memberRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        memberRecyclerView.setAdapter(memberAdapter);
        memberAdapter.startListening();
    }

    /**
     * Gets all group related events
     */
    private void fetchEvents() {
        final RecyclerView eventRecyclerView = (RecyclerView)findViewById(R.id.eventRecyclerView);
        Query query = DatabaseTools.getDbEventsReference().orderByChild("ownerGroup").equalTo(groupName);

        final FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(query, Event.class)
                        .build();

        FirebaseRecyclerAdapter<Event, ViewHolder> eventAdapter = new FirebaseRecyclerAdapter<Event, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, Event model) {
                holder.setTitle(model.getName());
                holder.setDescription(new Date(model.getStartTime())+"");
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openEventActivity(getRef(position).getKey());
                    }
                });
                holder.setImageView(R.drawable.ic_event_black_24dp);
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.view_row for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_row, parent, false);
                return new ViewHolder(view);
            }
        };

        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        eventRecyclerView.setAdapter(eventAdapter);
        eventAdapter.startListening();
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
                DatabaseTools.createEvent(groupName, System.currentTimeMillis());
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
                .setMessage("Do you really want to delete " + groupName + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (DatabaseTools.removeGroup(groupName)) {
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
                    DatabaseTools.removeUserFromGroup(DatabaseTools.getCurrentUsersUid(), groupName);
                    Toast.makeText(GroupActivity.this, "You left the group!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return true;

            case R.id.action_editgrp:
                if (isCurrentUserAdmin()) {
                    Intent intent = new Intent(GroupActivity.this, GroupEditActivity.class);
                    intent.putExtra(GroupEditActivity.KEY_EXTRA, groupName);
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
