package fighting_mongooses.walkhealthy.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.adapter.ChatAdapter;
import fighting_mongooses.walkhealthy.objects.ChatData;
import fighting_mongooses.walkhealthy.objects.Event;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;

public class MessagingActivity extends AppCompatActivity {

    public static final String KEY_EXTRA = "walkhealthy.KEY_MESSAGE";

    private DatabaseReference mReference;

    private EditText mChatInput;
    private Button mButtonSend;
    private ChatAdapter mAdapter;

    private String mUsername = "User";
    private String referenceId;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (!getIntent().hasExtra(KEY_EXTRA)) {
            this.finish();
        }

        queryUsername();
        mUserId = DatabaseTools.getCurrentUsersUid();

        if (getIntent().getStringExtra(KEY_EXTRA).startsWith("group")) {
            referenceId = getIntent().getStringExtra(KEY_EXTRA).substring(5);
            mReference = DatabaseTools.getDbGroupsReference().child(referenceId).child("messages");
            getSupportActionBar().setTitle(referenceId.toUpperCase() + " Messages");
        } else if (getIntent().getStringExtra(KEY_EXTRA).startsWith("event")) {
            referenceId = getIntent().getStringExtra(KEY_EXTRA).substring(5);
            mReference = DatabaseTools.getDbEventsReference().child(referenceId).child("messages");
            queryEventName(referenceId);
        }

        mButtonSend = findViewById(R.id.send);
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mChatInput = findViewById(R.id.chat_input);
        mChatInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return sendMessage();
            }
        });

        RecyclerView chat = findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        chat.setLayoutManager(llm);

        mAdapter = new ChatAdapter();
        chat.setAdapter(mAdapter);
        setupConnection();
    }

    private boolean sendMessage() {
        if (!mChatInput.getText().toString().equals("")) {
            ChatData data = new ChatData();
            data.setMessage(mChatInput.getText().toString());
            data.setId(mUserId);
            data.setName(mUsername);
            data.setDate(new Date().getTime());

            mReference.child(String.valueOf(new Date().getTime())).setValue(data);

            mChatInput.setText("");
        }
        return true;
    }

    // PRIVATE METHODS
    private void queryUsername() {
        DatabaseTools.getDbUsersReference().child(DatabaseTools.getCurrentUsersUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    mUsername = user.getUsername();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void queryEventName(String eventId) {
        DatabaseTools.getDbEventsReference().child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Event event = dataSnapshot.getValue(Event.class);
                if (event != null) {
                    getSupportActionBar().setTitle(event.getName() + " Messages");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupConnection() {
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                handleReturn(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void handleReturn(DataSnapshot dataSnapshot) {
        mAdapter.clearData();

        for(DataSnapshot item : dataSnapshot.getChildren()) {
            ChatData data = item.getValue(ChatData.class);
            mAdapter.addData(data);
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
