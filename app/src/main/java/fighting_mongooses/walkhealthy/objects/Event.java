package fighting_mongooses.walkhealthy.objects;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * The event owning group.
     */
    private String ownerGroup;

    /**
     * The event name.
     */
    private String name;

    /**
     * The event start time.
     */
    private long startTime;

    /**
     * List that contains at least start
     * and endpoint.
     */
    private Map<String,Object> route = new HashMap<>();

    /**
     * Map contains all attendees of this event.
     */
    private Map<String,Boolean> attendees = new HashMap<>();

    /**
     * Empty Class constructor. (Needed for Firebase)
     */
    public Event() {
        // DO NOT USE
    }

    /**
     * Class constructor.
     */
    public Event(String ownerGroup, String name, long startTime, Location... locations) {
        this.ownerGroup = ownerGroup;
        this.name = name;
        this.startTime = startTime;

        for (Location l : locations) {
            addLocation(l);
        }
    }

    /**
     * Getter for owning group
     *
     * @return      The group name
     */
    public String getOwnerGroup() {
        return ownerGroup;
    }

    /**
     * Getter for events name
     *
     * @return      The name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for events startTime
     *
     * @return      The startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Method to add a location to the route.
     */
    public void addLocation(Location location) {
        Map<String,Double> entry = new HashMap<>();
        entry.put("lat", location.getLatitude());
        entry.put("lng", location.getLongitude());
        route.put(route.size() + "", entry);
    }

    /**
     * Method to get all locations (the route)
     */
    public Map<String, Object> getRoute() {
        return new HashMap<>(route);
    }

    /**
     * Method to add attendee to the group.
     */
    public void addAttendee(String userId) {
        attendees.put(userId, true);
    }

    /**
     * Method to get all attendees the event has.
     *
     * @return      the attendees map.
     */
    public Map<String, Boolean> getAttendees() {
        return new HashMap<>(attendees);
    }
}
