package fighting_mongooses.walkhealthy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

/**
 * Apps main activity for user information
 *
 * This activity provides different actions for the users.
 *
 * @author Mario Fellinger
 */
public class MainActivity extends AppCompatActivity {

    private TableLayout usergrplayout, allgrplayout;

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

        usergrplayout = (TableLayout) findViewById(R.id.usergrplayout);
        allgrplayout = (TableLayout) findViewById(R.id.allgrplayout);

        fetchUsersGroups();
        fetchAllGroups();
    }

    /**
     * Fetches all the users groups from the firebase
     * database and displays and creates a tablerow
     * for each entry.
     */
    private void fetchUsersGroups() {
        DatabaseTools.getDbUsersReference().child(DatabaseTools.getCurrentUsersUid()).child("groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        usergrplayout.removeViewsInLayout(0, usergrplayout.getChildCount());
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            TableRow tr = new TableRow(MainActivity.this);
                            TextView tv = new TextView(MainActivity.this);
                            tv.setText("- " + child.getKey());
                            tv.setTag(child.getKey());
                            tv.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    final String groupName = view.getTag().toString();
                                    openGroupActivity(groupName);
                                }
                            });
                            tr.addView(tv);
                            usergrplayout.addView(tr);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    /**
     * Fetches all the groups from the firebase
     * database and displays and creates a tablerow
     * for each entry.
     */
    private void fetchAllGroups() {
        DatabaseTools.getDbGroupsReference().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        allgrplayout.removeViewsInLayout(0, allgrplayout.getChildCount());
                        for (final DataSnapshot child : dataSnapshot.getChildren()) {
                            TableRow tr = new TableRow(MainActivity.this);
                            TextView tv = new TextView(MainActivity.this);
                            tv.setText("- " + child.getKey());
                            tv.setTag(child.getKey());
                            tv.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    joinGroup(child.getKey().toString());
                                }
                            });
                            tr.addView(tv);
                            allgrplayout.addView(tr);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
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