package fighting_mongooses.walkhealthy.Objects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mario on 2/25/2018.
 */

public class Group {

    private String name;
    private String admin;
    private Map<String,Boolean> members = new HashMap<>();

    public Group(String name, String admin) {
        this.name = name;
        this.admin = admin;
    }

    public boolean addMember(String userID) {
        return members.put(userID, true);
    }

    public String getName() {
        return name;
    }

    public String getAdmin() {
        return admin;
    }
}
