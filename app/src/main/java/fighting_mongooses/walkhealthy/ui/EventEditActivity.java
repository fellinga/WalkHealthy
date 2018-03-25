package fighting_mongooses.walkhealthy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.Event;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

/**
 * GroupEdit dialog for group activities.
 *
 * This activity provides lets you create or modify groups.
 *
 * @author Mario Fellinger
 */
public class EventEditActivity extends AppCompatActivity {

    public static final String KEY_EXTRA = "walkhealthy.KEY_EVENTEDIT";
    private final int PLACE_PICKER_START = 1;
    private final int PLACE_PICKER_END = 2;

    private Event event;
    private String eventId;
    private EditText inputEventName;
    private CheckBox checkLow, checkMed, checkHigh;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button startLocBtn, endLocBtn;
    private TextView startLocationLabel, endLocationLabel;
    private Place endPlace;
    private Place startPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        }

        startLocBtn = findViewById(R.id.startLocBtn);
        endLocBtn = findViewById(R.id.endLocBtn);
        startLocationLabel = findViewById(R.id.startLocationLabel);
        endLocationLabel = findViewById(R.id.endLocationLabel);
        inputEventName = findViewById(R.id.inputEventName);
        checkLow = findViewById(R.id.checkLow);
        checkMed = findViewById(R.id.checkMed);
        checkHigh = findViewById(R.id.checkHigh);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);

        if (getIntent().hasExtra(KEY_EXTRA)) {
            if (getIntent().getStringExtra(KEY_EXTRA).startsWith("eventId")) {
                eventId = getIntent().getStringExtra(KEY_EXTRA).replace("eventId", "");
                getSupportActionBar().setTitle("Edit event");
                fetchEventData();
            } else {
                getSupportActionBar().setTitle("New event");
                event = new Event();
                event.setOwnerGroup(getIntent().getStringExtra(KEY_EXTRA));
                setCheckBoxes(0);
            }
        } else {
            finish();
        }

        startLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap(PLACE_PICKER_START);
            }
        });
        endLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap(PLACE_PICKER_END);
            }
        });
        checkLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCheckBoxes(0);
            }
        });
        checkMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCheckBoxes(1);
            }
        });
        checkHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCheckBoxes(2);
            }
        });
        datePicker.setMinDate(System.currentTimeMillis());
    }

    /**
     * Load existing event
     */
    private void fetchEventData() {
        // LOAD GROUP OBJECT
        DatabaseTools.getDbEventsReference().child(eventId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        event = dataSnapshot.getValue(Event.class);
                        if (event != null) {
                            inputEventName.setText(event.getName());
                            setCheckBoxes(event.getIntensity());
                            setStartPlace(event.getRouteLocation(0));
                            setEndPlace(event.getRouteLocation(1));
                            setDateTime();
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
     * Open map to pick start or end location
     */
    private void openMap(int requestCode) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        if (startPlace != null && requestCode == PLACE_PICKER_START) {
            builder.setLatLngBounds(new LatLngBounds(startPlace.getLatLng(), startPlace.getLatLng()));
        } else if (endPlace != null && requestCode == PLACE_PICKER_END) {
            builder.setLatLngBounds(new LatLngBounds(endPlace.getLatLng(), endPlace.getLatLng()));
        }
        try {
            startActivityForResult(builder.build(this), requestCode);
        } catch (Exception e) {

        }
    }

    /**
     * Returns from map activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_START) {
            if (resultCode == RESULT_OK) {
                setStartPlace(PlacePicker.getPlace(data, this));
            }
        } else if (requestCode == PLACE_PICKER_END) {
            if (resultCode == RESULT_OK) {
                setEndPlace(PlacePicker.getPlace(data, this));
            }
        }
    }

    /**
     * Sets the start location
     */
    private void setStartPlace(Place place) {
        startPlace = place;
        startLocationLabel.setText("Place: " + startPlace.getAddress());
    }

    /**
     * Sets the end location
     */
    private void setEndPlace(Place place) {
        endPlace = place;
        endLocationLabel.setText("Place: " + endPlace.getAddress());
    }

    /**
     * Sets the date and time picker
     */
    private void setDateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getStartTime());
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));
    }

    /**
     * Sets true or false values for the checkboxes
     */
    private void setCheckBoxes(int intensity) {
        switch (intensity) {
            // CASE DEFAULT COVERS CASE 0
            case 1: checkLow.setChecked(false);
                checkMed.setChecked(true);
                checkHigh.setChecked(false);
                break;
            case 2: checkLow.setChecked(false);
                checkMed.setChecked(false);
                checkHigh.setChecked(true);
                break;
            default: checkLow.setChecked(true);
                checkMed.setChecked(false);
                checkHigh.setChecked(false);
        }
    }

    /**
     * Takes the users input to create a new
     * group in the database.
     */
    private void createEvent() {
        String inputName = inputEventName.getText().toString();
        if (inputName.length() < 3) {
            Toast.makeText(EventEditActivity.this, "Event name to short!", Toast.LENGTH_SHORT).show();
            return;
        } else if (startPlace == null || endPlace == null) {
            Toast.makeText(EventEditActivity.this, "Please choose start and end location!", Toast.LENGTH_SHORT).show();
            return;
        }

        // SET NAME
        event.setName(inputName);
        // SET INTENSITY
        if (checkLow.isChecked()) event.setIntensity(0);
        if (checkMed.isChecked()) event.setIntensity(1);
        if (checkHigh.isChecked()) event.setIntensity(2);
        // SET START TIME
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                timePicker.getHour(), timePicker.getMinute(), 0);
        long startTime = calendar.getTimeInMillis();
        event.setStartTime(startTime);
        // SET START AND END
        event.addRouteLocation(startPlace, 0);
        event.addRouteLocation(endPlace, 1);

        // CREATE EVENT
        if (eventId != null && eventId.length() > 5) {
            DatabaseTools.updateEvent(event, eventId);
        } else {
            DatabaseTools.createEvent(event);
        }

        Toast.makeText(EventEditActivity.this, "Success.", Toast.LENGTH_SHORT).show();
        finish();
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
                createEvent();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
