package fighting_mongooses.walkhealthy.utilities;

import android.content.Context;
import android.net.Uri;

import com.firebase.geofire.GeoFire;
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
    public static final String GEOFIRE_PATH = "locations";
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

    /**
     * The geoFire object to create or get locations.
     */
    private static final GeoFire geoFire = new GeoFire(mDatabase.getReference().child(GEOFIRE_PATH));

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

    /**
     * Return the geoFire object to create or get locations.
     *
     * @return    The geoFire object.
     */
    public static GeoFire getGeoFire() { return geoFire; }

    //////////////////////////////////////////////
    // USER METHODS///////////////////////////////
    //////////////////////////////////////////////

    /**
     * Updates an users profile
     *
     * @param user   User object that should be modified
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
        return ref.putFile(uri);
    }

    /**
     * Gets a users profile picture based on id.
     *
     * @param uid    The user id
     * @param file   The file where to profile pic should be stored
     */
    public static FileDownloadTask getProfilePicture(String uid, File file) {
        final StorageReference ref = storageUserPicFolderRef.child(uid);
        return ref.getFile(file);
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
        final String userId = fbUser.getUid();

        // REMOVE STORAGE ENTRY'S
        storageUserPicFolderRef.child(userId).delete();

        // REMOVE USER FROM ALL GROUPS
        dbUsersRef.child(userId).child(GROUPS_PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot child : dataSnapshot.getChildren()) {
                    removeUserFromGroup(userId, child.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // DELETE USER FROM DATABASE
        dbUsersRef.child(userId).removeValue();
        // DELETE USER FROM AUTHENTICATION
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
        dbUsersRef.child(userId).child(GROUPS_PATH).child(groupName).setValue(true);
        dbGroupsRef.child(groupName).child("members").child(userId).setValue(true);
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
        dbUsersRef.child(userId).child(GROUPS_PATH).child(groupName).removeValue();
        dbGroupsRef.child(groupName).child("members").child(userId).removeValue();
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
        // REMOVE ALL USERS FROM GROUP
        dbUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    removeUserFromGroup(snapshot.getKey(), groupName);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // REMOVE ALL GROUP EVENTS
        dbEventsRef.orderByChild("ownerGroup").equalTo(groupName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            removeEvent(snapshot.getKey(), groupName);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        // REMOVE ACTUAL GROUP
        dbGroupsRef.child(groupName).removeValue();
        return true;
    }

    //////////////////////////////////////////////
    // EVENT METHODS /////////////////////////////
    //////////////////////////////////////////////

    /**
     * Adds the new event to the event reference.
     */
    public static void createEvent(final Event event) {
        // CREATE REFERENCE
        DatabaseReference ref = dbEventsRef.push();
        // ADD EVENT TO REFERENCE
        ref.setValue(event);
        // ADD THE EVENT KEY TO THE GROUP
        dbGroupsRef.child(event.getOwnerGroup()).child(EVENTS_PATH).child(ref.getKey()).setValue(true);
    }

    /**
     * Updates an existing event.
     */
    public static void updateEvent(final Event event, final String eventId) {
        dbEventsRef.child(eventId).setValue(event);
    }

    /**
     * Adds the current user to an event.
     *
     * @param eventId    The eventId where the user should be added.
     */
    public static void addUserToEvent(final String userId, final String eventId, boolean going) {
        dbEventsRef.child(eventId).child("attendees").child(userId).setValue(going);
        dbUsersRef.child(userId).child("events").child(eventId).setValue(going);
    }

    /**
     * Removes the current user from an event.
     *
     * @param eventId    The eventId where the user should be removed.
     */
    public static void removeUserFromEvent(final String userId, final String eventId) {
        dbEventsRef.child(eventId).child("attendees").child(userId).removeValue();
        dbUsersRef.child(userId).child("events").child(eventId).removeValue();
    }

    /**
     * Removes a event from the events reference.
     * Removes the event from the group node.
     *
     * @param eventId The event that should be removed.
     */
    public static void removeEvent(final String eventId, final String groupName) {
        // REMOVE ALL USERS FROM EVENT
        dbUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    removeUserFromEvent(snapshot.getKey(), eventId);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        dbEventsRef.child(eventId).removeValue();
        dbGroupsRef.child(groupName).child(EVENTS_PATH).child(eventId).removeValue();
    }
}