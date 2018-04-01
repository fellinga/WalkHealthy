package fighting_mongooses.walkhealthy.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.adapter.ViewHolder;
import fighting_mongooses.walkhealthy.objects.Group;
import fighting_mongooses.walkhealthy.utilities.AutoUpdate;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

/**
 * Apps main activity for user information
 *
 * This activity provides different actions for the users.
 *
 * @author Mario Fellinger
 */
public class MainActivity extends AppCompatActivity {

    private List<Group> nearGroups = new LinkedList<>();
    private RecyclerView.Adapter<ViewHolder> allGroupAdapter;
    private static final int REQUEST_ID_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        FloatingActionButton myFab = findViewById(R.id.addGrpBtn);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openNewGrpDialog();
            }
        });

        getCurrentLocation();
        fetchUsersGroups();
        fetchAllGroups();
    }

    /**
     * Gets the users current location.
     */
    private void getCurrentLocation() {
        LocationManager locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ID_LOCATION);
            return;
        }

        LocationListener locationListener = new LocationListener() {
            //start asking for gps locations, every ten seconds, or if device moves more than 5m
            @Override
            public void onLocationChanged(Location location) {
                fetchNearGroups(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    /**
     * returns when the user granted permission to location
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    /**
     * Fetches all the users groups from the firebase
     * database and displays and creates a tablerow
     * for each entry.
     */
    private void fetchUsersGroups() {
        final RecyclerView userGroupsRecyclerView = findViewById(R.id.userGroupsRecyclerView);
        Query query = DatabaseTools.getDbGroupsReference().
                        orderByChild("members/" + DatabaseTools.getCurrentUsersUid()).equalTo(true);

        final FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(query, Group.class)
                        .build();

        FirebaseRecyclerAdapter<Group, ViewHolder> userGroupAdapter = new FirebaseRecyclerAdapter<Group, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, final Group model) {
                holder.setTitle(model.getName());
                holder.setDescription(model.getMembers().size() + " members.");
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openGroupActivity(model.getName());
                    }
                });
                holder.setImageView(R.drawable.ic_group_black_24dp);
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

        userGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        userGroupsRecyclerView.setAdapter(userGroupAdapter);
        userGroupAdapter.startListening();
    }

    /**
     * Fetches all the near groups from the firebase
     * database and displays and creates a tablerow
     * for each entry.
     */
    private void fetchNearGroups(Location location) {
        final RecyclerView nearGroupsRecyclerView = findViewById(R.id.nearGroupsRecyclerView);
        allGroupAdapter = new RecyclerView.Adapter<ViewHolder>() {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_row, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                final Group group = nearGroups.get(position);

                holder.setTitle(group.getName());
                holder.setDescription(group.getMembers().size() + " members.");
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinGroup(group.getName());
                    }
                });
                holder.setImageView(R.drawable.ic_group_black_24dp);
            }

            @Override
            public int getItemCount() {
                return nearGroups.size();
            }
        };

        nearGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        nearGroupsRecyclerView.setAdapter(allGroupAdapter);

        nearGroups.clear();
        DatabaseTools.getGeoFire().queryAtLocation(
                new GeoLocation(location.getLatitude(), location.getLongitude()), 2.0)
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        DatabaseTools.getDbGroupsReference().child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Group group = dataSnapshot.getValue(Group.class);
                                for (int i = 0; i < nearGroups.size(); i++) {
                                    if (group.getName().equals(nearGroups.get(i).getName())) {
                                        return;
                                    }
                                }
                                nearGroups.add(group);
                                allGroupAdapter.notifyItemInserted(nearGroups.size() - 1);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });
    }

    /**
     * Fetches all the groups from the firebase
     * database and displays and creates a tablerow
     * for each entry.
     */
    private void fetchAllGroups() {
        final RecyclerView allGroupsRecyclerView = findViewById(R.id.allGroupsRecyclerView);
        Query query = DatabaseTools.getDbGroupsReference();

        final FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(query, Group.class)
                        .build();

        FirebaseRecyclerAdapter<Group, ViewHolder> allGroupAdapter = new FirebaseRecyclerAdapter<Group, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, final Group model) {
                holder.setTitle(model.getName());
                holder.setDescription(model.getMembers().size() + " members.");
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinGroup(getRef(position).getKey());
                    }
                });
                holder.setImageView(R.drawable.ic_group_black_24dp);
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

        allGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        allGroupsRecyclerView.setAdapter(allGroupAdapter);
        allGroupAdapter.startListening();
    }

    /**
     * Asks the user if he/she wants to join the group.
     *
     * @param groupName Name of the group
     */
    private void joinGroup(final String groupName) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Join group.")
                .setMessage("Do you want to join " + groupName + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        DatabaseTools.addUserToGroup(
                                DatabaseTools.getCurrentUsersUid(), groupName);
                        Toast.makeText(MainActivity.this, "Group joined!", Toast.LENGTH_SHORT).show();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * Open the specified group activity and sends the
     * group name with it.
     *
     * @param groupName Name of the group
     */
    private void openGroupActivity(String groupName) {
        // SEND THE GROUP NAME TO THE GROUP ACTIVITY
        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
        intent.putExtra(GroupActivity.KEY_EXTRA, groupName);
        startActivity(intent);
    }

    /**
     * Opens the new group dialog.
     */
    private void openNewGrpDialog() {
        Intent intent = new Intent(MainActivity.this, GroupEditActivity.class);
        startActivity(intent);
    }

    /**
     * This method logs the user out and redirects
     * to the main activity.
     */
    private void logoutUser() {
        // Launching the login activity
        DatabaseTools.logOffUser();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_logout:
                logoutUser();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        new AutoUpdate(this).execute();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Exit app")
                .setMessage("Do you want to close the app?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }
}