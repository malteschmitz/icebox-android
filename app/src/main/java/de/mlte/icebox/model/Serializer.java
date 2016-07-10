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

    public static JSONObject serializeConsumption(Consumption consumption) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("barcode", consumption.getBarcode());
        jsonObject.put("username", consumption.getUsername());
        return jsonObject;
    }

    public static User deserializeUser(final JSONObject json) throws JSONException {
        return new User() {{
            username = json.getString("username");
            avatarmail = json.getString("avatarmail");
            ledger = json.getInt("ledger");
            vds = json.getBoolean("vds");
        }};
    }

    public static List<User> deserializeUsers(JSONArray json) throws JSONException {
        List<User> users = new ArrayList<User>(json.length());
        for (int i = 0; i < json.length(); i++) {
            users.add(deserializeUser(json.getJSONObject(i)));
        }
        return users;
    }
}
