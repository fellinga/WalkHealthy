package fighting_mongooses.walkhealthy.utilities;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Map;

import fighting_mongooses.walkhealthy.listener.OnGetGroupListener;
import fighting_mongooses.walkhealthy.listener.OnGetUserListener;
import fighting_mongooses.walkhealthy.objects.Event;
import fighting_mongooses.walkhealthy.objects.Group;
import fighting_mongooses.walkhealthy.objects.User;

/**
 * Helper class for database requests
 *
 * This class provides useful static methods to add or request data from the database.
 * Important information and guideline for the database.
 * - Information about our database structure:
 * https://firebase.google.com/docs/database/android/structure-data#fanout
 * - How to: "firebase database":
 * https://firebase.google.com/docs/database/android/read-and-write
 *
 * @author Mario Fellinger
 */
public final class DatabaseTools {

    public static final String USERS_PATH = "users";
    public static final String GROUPS_PATH = "groups";
    public static final String EVENTS_PATH = "events";
    public static final String ALL_USERS_GROUP = "allusers";
    public static final String USER_PROFILE_PIC = "profilePics";

    /**
     * Reference to the firebase authentication.
     */
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Reference to the firebase storage.
     */
    private static final FirebaseStorage mStorage = FirebaseStorage.getInstance();

    /**
     * Specific reference to the profilePic folder in the firebase storage.
     */
    private static final StorageReference storageUserPicFolderRef = mStorage.getReference().child(USER_PROFILE_PIC);

    /**
     * Reference to the firebase database.
     */
    private static final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    /**
     * Specific reference to the USERS key in the firebase database.
     */
    private static final DatabaseReference dbUsersRef = mDatabase.getReference().child(USERS_PATH);

    /**
     * Specific reference to the GROUPS key in the firebase database.
     */
    private static final DatabaseReference dbGroupsRef = mDatabase.getReference().child(GROUPS_PATH);

    /**
     * Specific reference to the EVENTS key in the firebase database.
     */
    private static final DatabaseReference dbEventsRef = mDatabase.getReference().child(EVENTS_PATH);

    //////////////////////////////////////////////
    // GETTERS  //////////////////////////////////
    //////////////////////////////////////////////

    /**
     * Returns the firebase authentication object.
     *
     * @return    The firebase authentication
     */
    public static FirebaseAuth getFirebaseAuth() { return mAuth; }

    /**
     * Returns the currents user UID
     *
     * @return    The current user UID
     */
    public static String getCurrentUsersUid() { return mAuth.getCurrentUser().getUid(); }

    /**
     * Returns the currents user Email
     *
     * @return    The current user email
     */
    public static String getCurrentUsersEmail() { return mAuth.getCurrentUser().getEmail(); }

    /**
     * Returns the users profile pics storage reference.
     *
     * @return    The users pics reference.
     */
    public static StorageReference getStorageUserPicFolderRef() {
        return storageUserPicFolderRef;
    }

    /**
     * Returns the users database reference.
     *
     * @return    The users reference.
     */
    public static DatabaseReference getDbUsersReference() {
        return dbUsersRef;
    }

    /**
     * Returns the groups database reference.
     *
     * @return    The groups reference.
     */
    public static DatabaseReference getDbGroupsReference() { return dbGroupsRef; }

    /**
     * Returns the events database reference.
     *
     * @return    The events reference.
     */
    public static DatabaseReference getDbEventsReference() { return dbEventsRef; }

    //////////////////////////////////////////////
    // USER METHODS///////////////////////////////
    //////////////////////////////////////////////

    /**
     * Updates an users profile
     *
     * @param user   User object that should be modified
     * @return       true if user is updated false otherwise
     */
    public static void updateCurrentUser(final User user) {
        // TODO check for unique username - return false if duplicate
        dbUsersRef.child(getCurrentUsersUid()).setValue(user);
    }

    /**
     * Updates the current users profile picture.
     */
    public static UploadTask setProfilePicture(Uri uri) {
        final StorageReference ref = storageUserPicFolderRef.child(getCurrentUsersUid());
        final UploadTask uploadTask = ref.putFile(uri);

        return uploadTask;
    }

    /**
     * Gets a users profile picture based on id.
     *
     * @param uid    The user id
     * @param file   The file where to profile pic should be stored
     */
    public static FileDownloadTask getProfilePicture(String uid, File file) {
        final StorageReference ref = storageUserPicFolderRef.child(uid);
        final FileDownloadTask downloadTask = ref.getFile(file);

        return downloadTask;
    }

    public static void logOffUser() {
        mAuth.signOut();
    }

    /**
     * Deletes the current user in the firebase
     * auth and firebase database section.
     */
    public static void deleteUser() {
        final FirebaseUser fbUser = mAuth.getCurrentUser();
        // REMOVE USER FROM ALL GROUPS
        dbGroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    removeUserFromGroup(getCurrentUsersUid(), snapshot.getRef().getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // DELETE USER FROM DATABASE
        dbUsersRef.child(getCurrentUsersUid()).removeValue();
        // DELETE USER FROM AUTHENTICATION
        mAuth.signOut();
        fbUser.delete();
    }

    //////////////////////////////////////////////
    // GROUP METHODS//////////////////////////////
    //////////////////////////////////////////////

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

        // CREATE GROUP
        dbGroupsRef.child(group.getName()).setValue(group);

        // ADD EVERY USER TO THE GROUP
        for (Map.Entry e : group.getMembers().entrySet()) {
            addUserToGroup(e.getKey().toString(), group.getName());
        }
        return true;
    }

    /**
     * Updates an existing group in the group reference.
     *
     * @param group The group that should be added.
     * @return      True if group added false otherwise.
     */
    public static void updateGroup(final Group group) {
        // TODO make sure group exists
        dbGroupsRef.child(group.getName()).setValue(group);
    }

    /**
     * Changes an existing groups administrator.
     *
     * @param userId    The groups new administrator.
     * @param groupName The group name where we want the change.
     */
    public static void changeGroupAdmin(final String userId, final String groupName) {
        dbGroupsRef.child(groupName).child("admin").setValue(userId);
    }

    /**
     * Adds an existing user to an existing group. Both -
     * the user and the group reference are getting updated.
     *
     * @param userId    The user that should be added.
     * @param groupName The group name where we want the change.
     */
    public static void addUserToGroup(final String userId, final String groupName) {
        dbUsersRef.child(userId).child("groups").child(groupName).setValue("true");
        dbGroupsRef.child(groupName).child("members").child(userId).setValue("true");
    }

    /**
     * Removes an existing user from an existing group. Both -
     * the user and the group reference are getting removed.
     *
     * @param userId    The user where we want to remove the group.
     * @param groupName The group name where we want the change.
     */
    public static void removeUserFromGroup(final String userId, final String groupName) {
        // TODO if user is admin choose random new admin
        dbUsersRef.child(userId).child("groups").child(groupName).removeValue();
        dbGroupsRef.child(groupName).child("members").child(userId).removeValue();
    }

    /**
     * Adds an existing user to an existing group.
     *
     * @param eventId    The event that should be added.
     * @param groupName  The group name where we want the change.
     */
    public static void addEventToGroup(final String eventId, final String groupName) {
        dbGroupsRef.child(groupName).child("events").child(eventId).setValue("true");
    }

    /**
     * Removes an existing event from an existing group.
     *
     * @param eventId    The event that should be added.
     * @param groupName  The group name where we want the change.
     */
    public static void removeEventFromGroup(final String eventId, final String groupName) {
        dbGroupsRef.child(groupName).child("events").child(eventId).removeValue();
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
        if (groupName.equals(ALL_USERS_GROUP)) {
            return false;
        }

        // TODO only group owner should be able to remove

        dbUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

        dbGroupsRef.child(groupName).removeValue();
        return true;
    }

    //////////////////////////////////////////////
    // EVENT METHODS /////////////////////////////
    //////////////////////////////////////////////

    /**
     * Adds the new event to the event reference.
     *
     */
    public static void createEvent(final String groupName) {
        // CREATE REFERENCE
        DatabaseReference ref = dbEventsRef.push();
        // ADD EVENT TO REFERENCE
        ref.setValue(new Event(System.currentTimeMillis()));
        // ADD THE EVENT KEY TO THE GROUP
        addEventToGroup(ref.getKey(), groupName);
    }

    /**
     * Removes a event from the events reference.
     * Removes the event from the group node.
     *
     * @param eventId The event that should be removed.
     */
    public static void removeEvent(final String eventId, final String groupName) {
        dbEventsRef.child(eventId).removeValue();
        removeEventFromGroup(eventId, groupName);
    }

    //////////////////////////////////////////////
    // MISC //////////////////////////////////////
    //////////////////////////////////////////////

    /**
     * Returns the requested user as an user object.
     *
     * @param userId   The userId of the requested user.
     * @param listener Listener to perform the async request
     */
    public static void readUserData(String userId, final OnGetUserListener listener) {
        listener.onStart();
        dbUsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        dbGroupsRef.child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
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