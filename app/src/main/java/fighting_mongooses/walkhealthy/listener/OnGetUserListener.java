package fighting_mongooses.walkhealthy.listener;

import com.google.firebase.database.DatabaseError;

import fighting_mongooses.walkhealthy.objects.User;

public interface OnGetUserListener {
    void onStart();
    void onSuccess(User user);
    void onFailed(DatabaseError databaseError);
}