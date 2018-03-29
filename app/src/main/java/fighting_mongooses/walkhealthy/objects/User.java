package fighting_mongooses.walkhealthy.objects;

import java.util.HashMap;
import java.util.Map;

/**
 * User class that represents a single user entry in the database.
 *
 * This class helps to store and retrieve user objects from the database.
 *
 * @author Mario Fellinger
 */
public class User {

    /**
     * The users username.
     */
    private String username;

    /**
     * The users birthday.
     */
    private String birthday;

    /**
     * Contains all groups that the user has joined.
     */
    private Map<String,Object> groups = new HashMap<>();

    /**
     * Contains all groups that the user has joined.
     */
    private Map<String,Object> events = new HashMap<>();

    /**
     * Empty Class constructor. (Needed for Firebase)
     */
    public User() {
        // DO NOT USE
    }

    /**
     * Class constructor.
     */
    public User(String username, String birthday) {
        this.username = username;
        this.birthday = birthday;
    }

    /**
     * Getter for groups name
     *
     * @return      The users username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for users birthday.
     *
     * @return      The users birthday.
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     * Setter for user's birthday.
     * Assumes birthday has already been validated.
     */
    public void setBirthday(String birthday){
        this.birthday = birthday;
    }

    /**
     * Setter for user's username.
     * Assumes username has already been validated.
     */
    public void setUsername(String username){
        this.username = username;
    }

    /**
     * Method to add a group to the user.
     */
    public void addGroup(String grpName) {
        groups.put(grpName, true);
    }

    /**
     * Method to get all groups the user has joined.
     *
     * @return      the group map.
     */
    public Map<String,Object> getGroups() {
        return new HashMap<>(groups);
    }

    /**
     * Method to add a event to the user.
     */
    public void addEvent(String eventId) {
        events.put(eventId, true);
    }

    /**
     * Method to get all events the user has joined.
     *
     * @return      the event map.
     */
    public Map<String,Object> getEvents() {
        return new HashMap<>(events);
    }

}