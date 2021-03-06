package fighting_mongooses.walkhealthy.objects;

import com.google.android.gms.location.places.Place;

import java.util.HashMap;
import java.util.Map;

import fighting_mongooses.walkhealthy.utilities.LocationHelper;

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
     * The event start time.
     */
    private int intensity;

    /**
     * List that contains at least start
     * and endpoint.
     */
    private Map<String,Object> route = new HashMap<>();

    /**
     * Map contains all admins of this event.
     */
    private Map<String,Object> admins = new HashMap<>();

    /**
     * Map contains all attendees of this event.
     */
    private Map<String,Object> attendees = new HashMap<>();

    /**
     * Empty Class constructor. (Needed for Firebase)
     */
    public Event() {

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
     * Setter for owning group
     */
    public void setOwnerGroup(String ownerGroup) {
        this.ownerGroup = ownerGroup;
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
     * Setter for event name
     */
    public void setName(String name) {
        this.name = name;
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
     * Setter for events startTime
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Getter for events startTime
     *
     * @return      The startTime
     */
    public int getIntensity() {
        return intensity;
    }

    /**
     * Setter for intensity
     */
    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    /**
     * Method to add admins to the group.
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
     * Method to get all admins of the event has.
     *
     * @return      the admins map.
     */
    public Map<String, Object> getAdmins() {
        return new HashMap<>(admins);
    }

    /**
     * Method to add attendee to the group.
     */
    public void addAttendee(String userId) {
        attendees.put(userId, true);
    }

    /**l
     * Method to get all attendees the event has.
     *
     * @return      the attendees map.
     */
    public Map<String, Object> getAttendees() {
        return new HashMap<>(attendees);
    }

    /**
     * Method to add a location to the route.
     */
    public void addRouteLocation(Place place, int position) {
        Map<String,Object> location = new HashMap<>();
        location.put("address", place.getAddress());
        location.put("lat", place.getLatLng().latitude);
        location.put("lng", place.getLatLng().longitude);
        route.put("loc" + position + "", location);
    }

    /**
     * Method to get a location at a specific index.
     */
    public Place getRouteLocation(int position) {
        final Map<String,Object> location = (HashMap)route.get("loc" + position);
        if (location != null) {
            double lat = (double)location.get("lat");
            double lng =(double)location.get("lng");
            String address = (String)location.get("address");
            return LocationHelper.transformLatLngToPlace(lat, lng, address);
        }
        return LocationHelper.transformLatLngToPlace(0d, 0d, "Location not set.");
    }

    /**
     * Method to get a copy of all locations (the route)
     */
    public Map<String, Object> getRoute() {
        return new HashMap<>(route);
    }
}
