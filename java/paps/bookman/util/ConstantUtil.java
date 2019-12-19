package paps.bookman.util;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class ConstantUtil {

    public static final String baseURL = "https://maps.googleapis.com";


    public static final String USER_REF = "MechanicApp/Users";
    public static final String MECHANIC_REF = "MechanicApp/Mechanics";
    static final String UID = "UID";
    static final String USERNAME = "USERNAME";
    static final String ADDRESS = "ADDRESS";
    static final String EMAIL = "EMAIL";
    static final String LAT = "LAT";
    static final String LNG = "LNG";
    static final String PHONE = "PHONE";
    static final String PICTURE = "PICTURE";

    public static boolean hasValidFields(EditText... views) {
        for (EditText edt : views) {
            String s = edt.getText().toString();
            if (edt.getInputType() == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                if (!Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    return false;
                }
            }

            if (TextUtils.isEmpty(s)) {
                return false;
            }

            if (edt.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD && s.length() < 6) {
                return false;
            }

            if (edt.getInputType() == InputType.TYPE_CLASS_PHONE && s.length() < 10) {
                return false;
            }

        }
        return true;
    }

    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static MaterialDialog getDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .progress(true, 100)
                .title("Please wait...")
                .canceledOnTouchOutside(false)
                .theme(Theme.DARK)
                .build();
    }
}
