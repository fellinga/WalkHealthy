package fighting_mongooses.walkhealthy.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import fighting_mongooses.walkhealthy.R;
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
    private TableLayout attendeesGoingLayout, notGoingLayout;
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

        attendeesGoingLayout = (TableLayout) findViewById(R.id.attendeesGoingLayout);
        notGoingLayout = (TableLayout) findViewById(R.id.notGoingLayout);
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
                        addAttendees();
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
        attendeesGoingLayout.removeViews(0, attendeesGoingLayout.getChildCount());
        notGoingLayout.removeViews(0, notGoingLayout.getChildCount());

        for (Map.Entry<String, Boolean> entry : event.getAttendees().entrySet()) {
            if (entry.getValue()) {
                addUserToLayout(attendeesGoingLayout, entry.getKey());
            } else {
                addUserToLayout(notGoingLayout, entry.getKey());
            }
        }
    }

    /**
     * Adds the user to the specific layout.
     */
    private void addUserToLayout(final TableLayout layout, final String userId) {
        DatabaseTools.getDbUsersReference().child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        TableRow tr = new TableRow(EventActivity.this);
                        TextView tv = new TextView(EventActivity.this);
                        tv.setText("- " + user.getUsername());
                        tr.addView(tv);
                        layout.addView(tr);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
