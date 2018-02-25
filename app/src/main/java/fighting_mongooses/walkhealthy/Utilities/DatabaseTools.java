package fighting_mongooses.walkhealthy.Utilities;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import fighting_mongooses.walkhealthy.Objects.Group;
import fighting_mongooses.walkhealthy.Objects.User;

/**
 * Created by mario on 2/24/2018.
 */

public final class DatabaseTools {

    private static final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private static final DatabaseReference usersRef = mDatabase.getReference().child("users");
    private static final DatabaseReference groupsRef = mDatabase.getReference().child("groups");

    public static void addNewUser(final String userID, final User user) {
        Map<String,Object> userMap = new HashMap<>();
        userMap.put(userID, user);

        usersRef.updateChildren(userMap);
        addUserToGroup(userID, "allusers");
    }

    public static boolean addNewGroup(final Group group) {
        // TODO check if group exists
        Map<String,Object> groupMap = new HashMap<>();
        groupMap.put(group.getName(), group);

        groupsRef.updateChildren(groupMap);
        addUserToGroup(group.getAdmin(), group.getName());
        return true;
    }

    public static void changeGroupAdmin(final String userID, final String groupName) {
        groupsRef.child(groupName).child("admin").setValue(userID);
    }

    public static void addUserToGroup(final String userID, final String groupName) {
        usersRef.child(userID).child("groups").child(groupName).setValue("true");
        groupsRef.child(groupName).child("members").child(userID).setValue("true");
    }

    public static boolean removeGroup(final String groupName) {
        if (groupName.equals("allusers")) {
            return false;
        }

        // TODO only group owner shouuld be able to remove

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().child("groups").child(groupName).removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        groupsRef.child(groupName).removeValue();
        return true;
    }

}
