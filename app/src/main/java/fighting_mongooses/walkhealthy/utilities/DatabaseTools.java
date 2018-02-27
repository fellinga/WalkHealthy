package fighting_mongooses.walkhealthy.utilities;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import fighting_mongooses.walkhealthy.listener.OnGetGroupListener;
import fighting_mongooses.walkhealthy.listener.OnGetUserListener;
import fighting_mongooses.walkhealthy.objects.Group;
import fighting_mongooses.walkhealthy.objects.User;

/**
 * Helper class for database requests
 *
 * This class provides useful static methods to add or request data from the database.
 *
 * @author Mario Fellinger
 */
public final class DatabaseTools {

    /**
     * Reference to the firebase database.
     */
    private static final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    /**
     * Specific reference to the USERS key in the firebase database.
     */
    private static final DatabaseReference usersRef = mDatabase.getReference().child("users");

    /**
     * Specific reference to the GROUPS key in the firebase database.
     */
    private static final DatabaseReference groupsRef = mDatabase.getReference().child("groups");

    /**
     * Adds the new user to the user reference.
     * Also adds the new created user to the
     * *allusers* group.
     *
     * @param userID New Users ID stored as key
     * @param user   User object that should be added
     */
    public static void createUser(final String userID, final User user) {
        Map<String,Object> userMap = new HashMap<>();
        userMap.put(userID, user);

        usersRef.updateChildren(userMap);
        addUserToGroup(userID, "allusers");
    }

    /**
     * Adds the new group to the group reference.
     * Also - adds the new group admin as user to
     * the group.
     *
     * @param group The group that should be added.
     * @return      True if group added false otherwise.
     */
    public static boolean createGroup(final Group group) {
        if (group.getName().length() < 3) {
            return false;
        }

        // TODO check if group exists
        Map<String,Object> groupMap = new HashMap<>();
        groupMap.put(group.getName(), group);

        groupsRef.updateChildren(groupMap);
        addUserToGroup(group.getAdmin(), group.getName());
        return true;
    }

    /**
     * Changes an existing groups administrator.
     *
     * @param userID    The groups new administrator.
     * @param groupName The group name where we want the change.
     */
    public static void changeGroupAdmin(final String userID, final String groupName) {
        groupsRef.child(groupName).child("admin").setValue(userID);
    }

    /**
     * Adds an existing user to an existing group. Both -
     * the user and the group reference are getting updated.
     *
     * @param userID    The user that should be added.
     * @param groupName The group name where we want the change.
     */
    public static void addUserToGroup(final String userID, final String groupName) {
        usersRef.child(userID).child("groups").child(groupName).setValue("true");
        groupsRef.child(groupName).child("members").child(userID).setValue("true");
    }

    /**
     * Removes a group from the group reference.
     * Also - removes the group from every users
     * joined groups.
     *
     * @param groupName The group that should be removed.
     * @return          True if group removed false otherwise.
     */
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

    /**
     * Returns the requested user as an user object.
     *
     * @param userID   The userID of the requested user.
     * @param listener Listener to perform the async request
     */
    public static void readUserData(String userID, final OnGetUserListener listener) {
        listener.onStart();
        usersRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                listener.onSuccess(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailed(databaseError);
            }
        });
    }

    /**
     * Returns the requested group as a group object.
     *
     * @param groupName The groupName of the requested group.
     * @param listener  Listener to perform the async request
     */
    public static void readGroupData(String groupName, final OnGetGroupListener listener) {
        listener.onStart();
        groupsRef.child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Group group = dataSnapshot.getValue(Group.class);
                listener.onSuccess(group);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailed(databaseError);
            }
        });
    }
}