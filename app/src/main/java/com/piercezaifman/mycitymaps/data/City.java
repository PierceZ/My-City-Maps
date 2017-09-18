package com.piercezaifman.mycitymaps.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Model to represent a city from Firebase.
 * <p>
 * Created by piercezaifman on 2016-12-22.
 */

@IgnoreExtraProperties
public class City implements Parcelable {

    private String key;
    private String name;
    private String state;
    private String country;
    private List<String> maps;
    private Long timestamp;
    private String disclaimer;
    private String licenseLink;
    private String licenseOffline;

    public City() {
        //Empty constructor required by Firebase
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public String getLicenseLink() {
        return licenseLink;
    }

    public String getLicenseOffline() {
        return licenseOffline;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public List<String> getMaps() {
        return maps;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(key);
        out.writeString(name);
        out.writeString(state);
        out.writeString(country);
        out.writeStringList(maps == null ? new ArrayList<>() : maps);

        if (timestamp != null) {
            out.writeByte((byte) (0x00));
            out.writeLong(timestamp);
        } else {
            out.writeByte((byte) (0x01));
        }
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        public City[] newArray(int size) {
            return new City[size];
        }
    };

    private City(Parcel in) {
        this.key = in.readString();
        this.name = in.readString();
        this.state = in.readString();
        this.country = in.readString();

        this.maps = new ArrayList<>();
        in.readStringList(this.maps);

        if (in.readByte() == 0x00) {
            this.timestamp = in.readLong();
        }

    }

    @Override
    public String toString() {
        return getCountry() + "/" + getState() + "/" + getName();
    }

    public String toFormattedString() {
        return getCountry() + ", " + (getState() == null || getState().length() == 0 ? "" : (getState() + ", ")) + getName();
    }
}
