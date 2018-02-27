package fighting_mongooses.walkhealthy.listener;

import com.google.firebase.database.DatabaseError;

import fighting_mongooses.walkhealthy.objects.Group;

public interface OnGetGroupListener {
    void onStart();
    void onSuccess(Group group);
    void onFailed(DatabaseError databaseError);
}