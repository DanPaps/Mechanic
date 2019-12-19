package paps.bookman.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class Mechanic implements Parcelable {

    public static final Creator<Mechanic> CREATOR = new Creator<Mechanic>() {
        @Override
        public Mechanic createFromParcel(Parcel in) {
            return new Mechanic(in);
        }

        @Override
        public Mechanic[] newArray(int size) {
            return new Mechanic[size];
        }
    };
    private String mechanicName;
    private String mechanicLocation;
    private String key;
    private double latitude;
    private double longitude;

    public Mechanic() {
    }

    protected Mechanic(Parcel in) {
        mechanicName = in.readString();
        mechanicLocation = in.readString();
        key = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mechanicName);
        dest.writeString(mechanicLocation);
        dest.writeString(key);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getMechanicName() {
        return mechanicName;
    }

    public void setMechanicName(String mechanicName1) {
        this.mechanicName = mechanicName1;
    }

    public String getMechanicLocation() {
        return mechanicLocation;
    }

    public void setMechanicLocation(String mechanicLocation1) {
        this.mechanicLocation = mechanicLocation1;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
