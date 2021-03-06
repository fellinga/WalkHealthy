package fighting_mongooses.walkhealthy.objects;

import java.util.HashMap;
import java.util.Map;
import com.google.android.gms.location.places.Place;
import com.google.firebase.database.Exclude;

import fighting_mongooses.walkhealthy.utilities.LocationHelper;

/**
 * Group class that represents a single group entry in the database.
 *
 * This class helps to store and retrieve group objects from the database.
 *
 * @author Mario Fellinger
 */
public class Group {

    /**
     * The groups name.
     */
    private String name;

    /**
     * Map contains all admins of this group.
     */
    private Map<String,Object> admins = new HashMap<>();

    /**
     * Map contains all members of this group.
     */
    private Map<String,Object> members = new HashMap<>();

    /**
     * Map contains all events of this group.
     */
    private Map<String,Object> events = new HashMap<>();

    /**
     * Empty Class constructor. (Needed for Firebase)
     */
    public Group() {

    }

    /**
     * Getter for groups name
     *
     * @return      The groups name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for groups name
     *
     * @param name Name for group
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method to add one or more admins
     * to the group.
     */
    public void addAdmin(String userId) {
        admins.put(userId, true);
    }

    /**
     * Method to add members to the group.
     */
    public void removeAdmin(String userId) {
        admins.remove(userId);
    }

    /**
     * Method to get all members the group has.
     *
     * @return      the members map.
     */
    public Map<String,Object> getAdmins() {
        return new HashMap<>(admins);
    }

    /**
     * Method to add one or more members
     * to the group.
     */
    public void addMember(String userId) {
        members.put(userId, true);
    }

    /**
     * Method to add members to the group.
     */
    public void removeMember(String userId) {
        members.remove(userId);
    }

    /**
     * Method to get all members the group has.
     *
     * @return      the members map.
     */
    public Map<String,Object> getMembers() {
        return new HashMap<>(members);
    }

    /**
     * Method to add event to the group.
     */
    public void addEvent(String eventId) {
        events.put(eventId, true);
    }

    /**
     * Method to get all events the group has.
     *
     * @return      the events map.
     */
    public Map<String,Object> getEvents() {
        return new HashMap<>(events);
    }
}
