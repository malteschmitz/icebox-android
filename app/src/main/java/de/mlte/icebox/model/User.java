package de.mlte.icebox.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    protected User() {}

    protected User(Parcel in) {
        username = in.readString();
        avatarmail = in.readString();
        ledger = in.readInt();
        vds = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(avatarmail);
        dest.writeInt(ledger);
        dest.writeByte((byte) (vds ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

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

    public String getAvatarmail() {
        return avatarmail;
    }

    public int getLedger() {
        return ledger;
    }

    public String getUsername() {
        return username;
    }

    public boolean isVds() {
        return vds;
    }

    protected String username;
    protected String avatarmail;
    protected int ledger;
    protected boolean vds;
}
