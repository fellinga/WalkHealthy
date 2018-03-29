package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.adapter.ViewHolder;
import fighting_mongooses.walkhealthy.objects.Group;
import fighting_mongooses.walkhealthy.objects.User;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.addGrpBtn);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openNewGrpDialog();
            }
        });

        fetchUsersGroups();
        fetchAllGroups();
    }

    /**
     * Fetches all the users groups from the firebase
     * database and displays and creates a tablerow
     * for each entry.
     */
    private void fetchUsersGroups() {
        final RecyclerView userGroupsRecyclerView = (RecyclerView)findViewById(R.id.userGroupsRecyclerView);
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
     * Fetches all the groups from the firebase
     * database and displays and creates a tablerow
     * for each entry.
     */
    private void fetchAllGroups() {
        final RecyclerView allGroupsRecyclerView = (RecyclerView)findViewById(R.id.allGroupsRecyclerView);
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