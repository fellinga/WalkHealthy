package fighting_mongooses.walkhealthy.objects;

import java.util.Date;

/**
 * Class responsible to hold the name and the message to the user
 * to send to firebase
 */
public class ChatData {

    private String mName;
    private String mId;
    private String mMessage;
    private long mDate;

    public ChatData() {
        // empty constructor
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public void setId(String id) {
        mId = id;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }
}
