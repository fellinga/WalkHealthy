package fighting_mongooses.walkhealthy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fighting_mongooses.walkhealthy.Objects.Group;
import fighting_mongooses.walkhealthy.Utilities.DatabaseTools;

public class MainActivity extends AppCompatActivity {

    private TableLayout grplayout;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.addGrpBtn);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewGroup();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        user = mAuth.getCurrentUser();
        grplayout = (TableLayout) findViewById(R.id.grplayout);

        fetchUsersGroups();
    }

    private void fetchUsersGroups() {
        DatabaseReference dataRef = mDatabase.getReference();

        dataRef.child("users").child(user.getUid()).child("groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        grplayout.removeViewsInLayout(0, grplayout.getChildCount());
                        for (DataSnapshot child : snapshot.getChildren()) {
                            TableRow tr = new TableRow(MainActivity.this);
                            TextView tv = new TextView(MainActivity.this);
                            tv.setText("- " + child.getKey());
                            tv.setTag(child.getKey());
                            tv.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    final String groupName = view.getTag().toString();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Delete group.")
                                            .setMessage("Do you really want to delete " + groupName + "?")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (DatabaseTools.removeGroup(groupName)) {
                                                        Toast.makeText(MainActivity.this, "Group deleted.", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Could not delete group.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }})
                                            .setNegativeButton(android.R.string.no, null).show();
                                }
                            });
                            tr.addView(tv);
                            grplayout.addView(tr);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void addNewGroup() {
        final AlertDialog.Builder inputAlert = new AlertDialog.Builder(this);
        inputAlert.setTitle("Enter Group Name");
        final EditText userInput = new EditText(this);
        inputAlert.setView(userInput);
        inputAlert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = userInput.getText().toString();
                Group grp = new Group(groupName, user.getUid());
                if (DatabaseTools.addNewGroup(grp)) {
                    Toast.makeText(MainActivity.this, "Group created.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Could not create group.", Toast.LENGTH_SHORT).show();
                }
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

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
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

    private void logoutUser() {
        // Launching the login activity
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}