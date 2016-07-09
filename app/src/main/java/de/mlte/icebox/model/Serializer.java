package de.mlte.icebox.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Serializer {
    public static Drink deserializeDrink(final JSONObject json) throws JSONException {
        return new Drink() {{
            name = json.getString("name");
            barcode = json.getString("barcode");
            fullprice = json.getInt("fullprice");
            discountprice = json.getInt("discountprice");
            quantity = json.getInt("quantity");
            empties = json.getInt("empties");
        }};
    }

    public static List<Drink> deserializeDrinks(final JSONArray json) throws JSONException {
        List<Drink> drinks = new ArrayList<>(json.length());
        for (int i = 0; i < json.length(); i++) {
            drinks.add(deserializeDrink(json.getJSONObject(i)));
        }
        return drinks;
    }
}
