package fighting_mongooses.walkhealthy.Objects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mario on 2/25/2018.
 */

public class User {

    private String username;
    private String birthday;
    private Map<String,Boolean> groups = new HashMap<>();

    public User(String username, String birthday) {
        this.username = username;
        this.birthday = birthday;
    }

    public boolean addGroup(String grpName) {
        return groups.put(grpName, true);
    }

    public String getUsername() {
        return username;
    }

    public String getBirthday() {
        return birthday;
    }
}
