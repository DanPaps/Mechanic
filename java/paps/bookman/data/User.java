package paps.bookman.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class User implements Parcelable {
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private String uid;
    private String username;
    private String email;
    private String phone;
    private String picture;
    private String address;
    private double lat;
    private double lng;

    public User() {
    }

    public User(String uid, String username, String email, String phone, String picture, String address, double lat, double lng) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.picture = picture;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    protected User(Parcel in) {
        uid = in.readString();
        username = in.readString();
        email = in.readString();
        phone = in.readString();
        picture = in.readString();
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(picture);
        dest.writeString(address);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
