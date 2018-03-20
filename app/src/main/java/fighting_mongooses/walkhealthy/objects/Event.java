package fighting_mongooses.walkhealthy.objects;

import java.util.HashMap;
import java.util.Map;

/**
 * Event class that represents a single event entry in the database.
 *
 * This class helps to store and retrieve event objects from the database.
 *
 * @author Mario Fellinger
 */
public class Event {

    /**
     * The event name.
     */
    private long timestamp;

    /**
     * Map contains all attendees of this event.
     */
    private Map<String,String> attendees = new HashMap<>();

    /**
     * Empty Class constructor. (Needed for Firebase)
     */
    public Event() {
        // DO NOT USE
    }

    /**
     * Class constructor.
     */
    public Event(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Getter for events timestamp
     *
     * @return      The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Method to add attendee to the group.
     */
    public void addAttendee(String userId) {
        attendees.put(userId, "true");
    }

    /**
     * Method to get all attendees the event has.
     *
     * @return      the attendees map.
     */
    public Map<String, String> getAttendees() {
        return new HashMap<>(attendees);
    }
}
