package de.mlte.icebox.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Drink implements Parcelable {
    protected String name;
    protected String barcode;
    protected int fullprice;
    protected int discountprice;
    protected int quantity;
    protected int empties;

    public Drink() {};

    public String getName() {
        return name;
    }

    public String getBarcode() {
        return barcode;
    }

    public int getFullprice() {
        return fullprice;
    }

    public int getDiscountprice() {
        return discountprice;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getEmpties() {
        return empties;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(barcode);
        dest.writeInt(fullprice);
        dest.writeInt(discountprice);
        dest.writeInt(quantity);
        dest.writeInt(empties);
    }

    private Drink(Parcel in) {
        name = in.readString();
        barcode = in.readString();
        fullprice = in.readInt();
        discountprice = in.readInt();
        quantity = in.readInt();
        empties = in.readInt();
    }

    public static final Parcelable.Creator<Drink> CREATOR = new Parcelable.Creator<Drink>() {
        public Drink createFromParcel(Parcel in) {
            return new Drink(in);
        }

        public Drink[] newArray(int size) {
            return new Drink[size];
        }
    };


}
