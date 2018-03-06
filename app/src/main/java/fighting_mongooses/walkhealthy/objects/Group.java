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
     * Map contains all members of this group.
     */
    private Map<String,String> members = new HashMap<>();

    /**
     * Empty Class constructor. (Needed for Firebase)
     */
    public Group() {
        // DO NOT USE
    }

    /**
     * Class constructor.
     */
    public Group(String name, String admin) {
        this.name = name;
        this.admin = admin;
        addMember(admin);
    }

    /**
     * Method to add members to the group.
     */
    public void addMember(String userID) {
        members.put(userID, "true");
    }

    /**
     * Method to get all members the group has.
     *
     * @return      the members map.
     */
    public Map<String, String> getMembers() {
        return new HashMap<>(members);
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
     * Getter for groups administrator.
     *
     * @return      The groups administrator
     */
    public String getAdmin() {
        return admin;
    }
}
