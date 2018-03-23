package fighting_mongooses.walkhealthy.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.adapter.ViewHolder;
import fighting_mongooses.walkhealthy.objects.Event;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

/**
 * Event activity for event activities.
 *
 * This activity provides different actions for different events.
 *
 * @author Mario Fellinger
 */
public class EventActivity extends AppCompatActivity {

    public static final String KEY_EXTRA = "walkhealthy.KEY_EVENT";

    private Event event;
    private String eventId;
    private Toolbar toolbar;
    private Button attendEvent, notAttendEvent, removeUserEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // MAKE SURE EVENT INFO WAS GIVEN
        // OTHERWISE QUIT.
        if (!getIntent().hasExtra(KEY_EXTRA)) {
            this.finish();
        }

        eventId = getIntent().getStringExtra(KEY_EXTRA);
        attendEvent = (Button) findViewById(R.id.attendEvent);
        notAttendEvent = (Button) findViewById(R.id.notAttendEvent);
        removeUserEvent = (Button) findViewById(R.id.removeUserEvent);

        attendEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseTools.addUserToEvent(eventId, true);
            }
        });
        notAttendEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseTools.addUserToEvent(eventId, false);
            }
        });
        removeUserEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseTools.removeUserFromEvent(eventId);
            }
        });

        fetchEventData();
        addAttendees();
    }

    /**
     * Gets the event object
     */
    private void fetchEventData() {
        DatabaseTools.getDbEventsReference().child(eventId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Event fbEvent = dataSnapshot.getValue(Event.class);
                    if (fbEvent != null) {
                        EventActivity.this.event = fbEvent;
                        toolbar.setTitle(event.getName().toUpperCase());
                        toolbar.setSubtitle("Walk Healthy Event");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    /**
     * Gets all event attendees.
     */
    private void addAttendees() {
        final RecyclerView attendingRecyclerView = (RecyclerView)findViewById(R.id.attendingRecyclerView);
        final RecyclerView notAttendingRecyclerView = (RecyclerView)findViewById(R.id.notAttendingRecyclerView);

        fetchAttendees(attendingRecyclerView, true);
        fetchAttendees(notAttendingRecyclerView, false);
    }

    private void fetchAttendees(final RecyclerView attendeesRecyclerView, final boolean going) {
        Query query = DatabaseTools.getDbUsersReference().orderByChild("events/" + eventId).equalTo(going);

        final FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, ViewHolder> adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {
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

        attendeesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        attendeesRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    /**
     * Deletes the specified event and closes
     * the activity.
     */
    private void deleteEvent() {
        new AlertDialog.Builder(EventActivity.this)
                .setTitle("Delete group.")
                .setMessage("Do you really want to delete " + event.getName() + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseTools.removeEvent(eventId, event.getOwnerGroup());
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_deleteEvent:
                deleteEvent();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
