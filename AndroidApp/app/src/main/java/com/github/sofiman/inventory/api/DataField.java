package com.github.sofiman.inventory.api;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

public class DataField implements Parcelable {

    private @DrawableRes int icon;
    private String value;
    private String hint;
    private String id;

    public DataField(@DrawableRes int icon, String value, String hint) {
        this.icon = icon;
        this.value = value;
        this.hint = hint;
        this.id = hint.toLowerCase().replaceAll(" ", "_");
    }

    public DataField(Parcel in) {
        icon = in.readInt();
        value = in.readString();
        hint = in.readString();
        id = in.readString();
    }

    public DataField id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public @DrawableRes int getIcon() {
        return icon;
    }

    public void setIcon(@DrawableRes int icon) {
        this.icon = icon;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(icon);
        dest.writeString(value);
        dest.writeString(hint);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<DataField> CREATOR = new Parcelable.Creator<DataField>()
    {
        public DataField createFromParcel(Parcel in)
        {
            return new DataField(in);
        }
        public DataField[] newArray(int size)
        {
            return new DataField[size];
        }
    };

    @Override
    public String toString() {
        return "DataField{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                ", hint='" + hint + '\'' +
                ", icon=" + icon +
                '}';
    }
}
