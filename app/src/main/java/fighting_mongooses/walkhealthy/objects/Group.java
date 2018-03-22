package fighting_mongooses.walkhealthy.objects;

import java.util.HashMap;
import java.util.Map;

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
     * The groups administrator.
     */
    private String admin;

    /**
     * The event intensity.
     */
    private int intensity;

    /**
     * Map contains all members of this group.
     */
    private Map<String,Boolean> members = new HashMap<>();

    /**
     * Map contains all events of this group.
     */
    private Map<String,Boolean> events = new HashMap<>();

    /**
     * Empty Class constructor. (Needed for Firebase)
     */
    public Group() {
        // DO NOT USE
    }

    /**
     * Class constructor.
     */
    public Group(String admin) {
        this.name = "Group";
        this.admin = admin;
        addMember(admin);
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
     * Getter for groups administrator.
     *
     * @return      The groups administrator
     */
    public String getAdmin() {
        return admin;
    }

    /**
     * Getter for groups intensity.
     *
     * @return      The groups intensity
     */
    public int getIntensity() {
        return intensity;
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
    public Map<String, Boolean> getMembers() {
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
    public Map<String, Boolean> getEvents() {
        return new HashMap<>(events);
    }
}
