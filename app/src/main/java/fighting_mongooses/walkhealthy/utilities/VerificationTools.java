package fighting_mongooses.walkhealthy.utilities;

import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Fragment;
import android.app.Activity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fighting_mongooses.walkhealthy.R;
import fighting_mongooses.walkhealthy.objects.User;
import fighting_mongooses.walkhealthy.utilities.DatabaseTools;


/**
 * Created by Gillenwater on 3/14/2018.
 */

public final class VerificationTools {

    // Data Validation Constants
    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MAX_USERNAME_LENGTH = 128;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 64;


    /**
     * Verifies the input of a Birthday. Checks if it satisfies a simple REGEX of ##/##/####
     * @author Jake Gillenwater
     * @param birthday The date string to check against
     * @return  True - if valid, False - if there is a problem with it.
     */
    public static boolean confirmBirthday(String birthday){
        // TODO: Verify based on age as well. No one over 100, under TBT, or from the future
        // TODO: Verify this is an actual date (i.e. no 67th month, no June 34th)
        Pattern p = Pattern.compile("[\\d]{1,2}/[\\d]{1,2}/[\\d]{4}");
        Matcher m = p.matcher(birthday);
        return m.matches();
    }

    /**
     * Verifies the input of an Email address. Checks if satisfies a simple REGEX.
     * @author Jake Gillenwater
     * @param email     The Email to check against
     * @return      True - if valid, False - if there is a problem with it.
     */
    public static boolean confirmEmail(String email){
        Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * Verifies the input of a password. Checks if is a proper length.
     * @author Jake Gillenwater
     * @param password The password to check against
     * @return  True - if valid, False- if there is a problem with it.
     */
    public static boolean confirmPassword(String password){
        return (password.length() >= MIN_PASSWORD_LENGTH && password.length() <= MAX_PASSWORD_LENGTH);
    }

    /**
     * Verifies the input of a user name. Checks if contains a value, and has a proper length.
     * @author Jake Gillenwater
     * @param username  The username to check against
     * @return  True - if valid, False - if there is a problem with it.
     */
    public static boolean confirmUsername(String username){
        return (!username.isEmpty() && username.length() > MIN_USERNAME_LENGTH && username.length() <= MAX_USERNAME_LENGTH);
    }




}
