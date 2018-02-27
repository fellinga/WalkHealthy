package fighting_mongooses.walkhealthy.utilities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
     * Reference to the firebase authentication.
     */
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

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
     * @param user   User object that should be added
     */
    public static void createUser(final User user) {
        final FirebaseUser fbUser = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getUsername()).build();
        fbUser.updateProfile(profileUpdates);
        fbUser.sendEmailVerification();

        usersRef.child(fbUser.getUid()).setValue(user);
        addUserToGroup(fbUser.getUid(), "allusers");
    }

    /**
     * Updates an existing user based on his
     * userId.
     *
     * @param userId Users ID to find the user
     * @param user   User object that should be modified
     */
    public static void updateUser(final String userId, final User user) {
        Map<String,Object> userMap = new HashMap<>();
        userMap.put(userId, user);
        usersRef.updateChildren(userMap);
    }

    /**
     * Deletes the current user in the firebase
     * auth and firebase database section.
     */
    public static void deleteUser() {
        final FirebaseUser fbUser = mAuth.getCurrentUser();
        // REMOVE USER FROM ALL GROUPS
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    removeUserFromGroup(fbUser.getUid(), snapshot.getRef().getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // DELETE USER FROM DATABASE
        usersRef.child(fbUser.getUid()).removeValue();
        // DELETE USER FROM AUTHENTICATION
        mAuth.signOut();
        fbUser.delete();
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
     * @param userId    The groups new administrator.
     * @param groupName The group name where we want the change.
     */
    public static void changeGroupAdmin(final String userId, final String groupName) {
        groupsRef.child(groupName).child("admin").setValue(userId);
    }

    /**
     * Adds an existing user to an existing group. Both -
     * the user and the group reference are getting updated.
     *
     * @param userId    The user that should be added.
     * @param groupName The group name where we want the change.
     */
    public static void addUserToGroup(final String userId, final String groupName) {
        usersRef.child(userId).child("groups").child(groupName).setValue("true");
        groupsRef.child(groupName).child("members").child(userId).setValue("true");
    }

    /**
     * Removes an existing user from an existing group. Both -
     * the user and the group reference are getting removed.
     *
     * @param userId    The user where we want to remove the group.
     * @param groupName The group name where we want the change.
     */
    public static void removeUserFromGroup(final String userId, final String groupName) {
        // TODO if user is admin remove group
        usersRef.child(userId).child("groups").child(groupName).removeValue();
        groupsRef.child(groupName).child("members").child(userId).removeValue();
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

        // TODO only group owner should be able to remove

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
     * @param userId   The userId of the requested user.
     * @param listener Listener to perform the async request
     */
    public static void readUserData(String userId, final OnGetUserListener listener) {
        listener.onStart();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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