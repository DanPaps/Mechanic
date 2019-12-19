package paps.bookman.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import paps.bookman.data.User;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class MechanicUserPrefs {
    public static final String SHARED_PREFS = "SHARED_PREFS";
    public final FirebaseDatabase db;
    public final FirebaseAuth auth;
    public final FirebaseStorage storage;
    private final Context context;
    private final SharedPreferences prefs;
    private boolean isLoggedIn = false;
    private String uid;
    private String username;
    private String email;
    private String phone;
    private String picture;
    private String address;
    private double lat;
    private double lng;

    public MechanicUserPrefs(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        this.db = FirebaseDatabase.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.storage = FirebaseStorage.getInstance();

        // Init fields
        uid = prefs.getString(ConstantUtil.UID, null);
        username = prefs.getString(ConstantUtil.USERNAME, null);
        email = prefs.getString(ConstantUtil.EMAIL, null);
        phone = prefs.getString(ConstantUtil.PHONE, null);
        picture = prefs.getString(ConstantUtil.PICTURE, null);
        address = prefs.getString(ConstantUtil.ADDRESS, null);
        lat = prefs.getFloat(ConstantUtil.LAT, 0.0f);
        lng = prefs.getFloat(ConstantUtil.LNG, 0.0f);

        isLoggedIn = auth.getCurrentUser() != null;

        if (isLoggedIn) {
            uid = prefs.getString(ConstantUtil.UID, null);
            username = prefs.getString(ConstantUtil.USERNAME, null);
            email = prefs.getString(ConstantUtil.EMAIL, null);
            phone = prefs.getString(ConstantUtil.PHONE, null);
            picture = prefs.getString(ConstantUtil.PICTURE, null);
            address = prefs.getString(ConstantUtil.ADDRESS, null);
            lat = prefs.getFloat(ConstantUtil.LAT, 0.0f);
            lng = prefs.getFloat(ConstantUtil.LNG, 0.0f);
        }
    }

    public User getUser() {
        return new User(uid, username, email, phone, picture, address, lat, lng);
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        prefs.edit().putString(ConstantUtil.USERNAME, username).apply();
    }

    public String getEmail() {
        return email;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
        prefs.edit().putString(ConstantUtil.PICTURE, picture).apply();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        prefs.edit().putString(ConstantUtil.ADDRESS, address).apply();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        prefs.edit().putString(ConstantUtil.PHONE, phone).apply();
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
        prefs.edit().putFloat(ConstantUtil.LAT, (float) lat).apply();
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
        prefs.edit().putFloat(ConstantUtil.LNG, (float) lng).apply();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void updateUser(User user) {
        isLoggedIn = user.getUid() != null;

        uid = user.getUid();
        username = user.getUsername();
        email = user.getEmail();
        phone = user.getPhone();
        address = user.getAddress();
        lat = user.getLat();
        lng = user.getLng();
        picture = user.getPicture();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ConstantUtil.UID, uid);
        editor.putString(ConstantUtil.USERNAME, username);
        editor.putString(ConstantUtil.EMAIL, email);
        editor.putString(ConstantUtil.PHONE, phone);
        editor.putString(ConstantUtil.ADDRESS, address);
        editor.putString(ConstantUtil.PICTURE, picture);
        editor.putFloat(ConstantUtil.LAT, (float) lat);
        editor.putFloat(ConstantUtil.LNG, (float) lng);
        editor.apply();
    }

    public void logOut() {
        auth.signOut();
        isLoggedIn = false;

        uid = null;
        username = null;
        email = null;
        phone = null;
        address = null;
        lat = 0.00;
        lng = 0.00;
        picture = null;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ConstantUtil.UID, uid);
        editor.putString(ConstantUtil.USERNAME, username);
        editor.putString(ConstantUtil.EMAIL, email);
        editor.putString(ConstantUtil.PHONE, phone);
        editor.putString(ConstantUtil.ADDRESS, address);
        editor.putString(ConstantUtil.PICTURE, picture);
        editor.putFloat(ConstantUtil.LAT, (float) lat);
        editor.putFloat(ConstantUtil.LNG, (float) lng);
        editor.apply();
    }
}
