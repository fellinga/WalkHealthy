package fighting_mongooses.walkhealthy.Utilities;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by mario on 2/24/2018.
 */

public final class DatabaseTools {

    private static final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private static final DatabaseReference users = mDatabase.getReference().child("users");
    private static final DatabaseReference groups = mDatabase.getReference().child("groups");

    public static boolean addNewGroup(final String userID, final String groupName) {
        // TODO check if group exists
        // Adding an user to a non existing group creates the group.
        addUserToGroup(groupName, userID);
        changeGroupAdmin(groupName, userID);
        return true;
    }

    public static void changeGroupAdmin(final String groupName, final String newAdmin) {
        groups.child(groupName).child("admin").setValue(newAdmin);
    }

    public static void addUserToGroup(final String groupName, final String newMember) {
        users.child(newMember).child("groups").child(groupName).setValue("true");
        groups.child(groupName).child("members").child(newMember).setValue("true");
    }

    public static boolean removeGroup(final String groupName) {
        if (groupName.equals("allusers")) {
            return false;
        }

        // TODO only group owner shouuld be able to remove

        users.addListenerForSingleValueEvent(new ValueEventListener() {
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

        groups.child(groupName).removeValue();
        return true;
    }

}
