package fighting_mongooses.walkhealthy.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

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
    private TextView eventInfo;
    private Button attendEvent, notAttendEvent, removeUserEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        toolbar = findViewById(R.id.toolbar);
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
        eventInfo = findViewById(R.id.eventInfo);
        attendEvent = findViewById(R.id.attendEvent);
        notAttendEvent = findViewById(R.id.notAttendEvent);
        removeUserEvent = findViewById(R.id.removeUserEvent);

        attendEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseTools.addUserToEvent(DatabaseTools.getCurrentUsersUid(), eventId, true);
            }
        });
        notAttendEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseTools.addUserToEvent(DatabaseTools.getCurrentUsersUid(), eventId, false);
            }
        });
        removeUserEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseTools.removeUserFromEvent(DatabaseTools.getCurrentUsersUid(), eventId);
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
                        setEventInfo();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    /**
     * TODO CHANGE THE TEXT VIEW
     * Sets all the event Infos.
     */
    private void setEventInfo() {
        eventInfo.setText("Intensity: " + event.getIntensity() +
                        "\nDate: " + new Date(event.getStartTime()) +
                        "\nStart: " + event.getRouteLocation(0).getAddress() +
                        "\nEnd: " + event.getRouteLocation(1).getAddress());
    }

    /**
     * Gets all event attendees.
     */
    private void addAttendees() {
        final RecyclerView attendingRecyclerView = findViewById(R.id.attendingRecyclerView);
        final RecyclerView notAttendingRecyclerView = findViewById(R.id.notAttendingRecyclerView);

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

            case R.id.action_messages:
                Intent intent = new Intent(EventActivity.this, MessagingActivity.class);
                intent.putExtra(MessagingActivity.KEY_EXTRA, "event" + eventId);
                startActivityForResult(intent, 0);
                return true;

            case R.id.action_editEvent:
                Intent editIntent = new Intent(EventActivity.this, EventEditActivity.class);
                editIntent.putExtra(EventEditActivity.KEY_EXTRA, "eventId" + eventId);
                startActivity(editIntent);
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
