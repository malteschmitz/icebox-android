package de.mlte.icebox.model;

import org.json.JSONObject;

public class Drink {
    protected String name;
    protected String barcode;
    protected int fullprice;
    protected int discountprice;
    protected int quantity;
    protected int empties;

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
}
