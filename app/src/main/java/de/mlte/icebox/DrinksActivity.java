package de.mlte.icebox;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Parcelable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mlte.icebox.model.Drink;

public class DrinksActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drinks);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(IceboxActivity.DRINKS_MESSAGE);

        List<Map<String, String>> drinksList = new ArrayList<Map<String, String>>(parcelables.length);

        for (Parcelable parcelable: parcelables) {
            Drink drink = (Drink) parcelable;
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", drink.getName());
            map.put("price", Integer.toString(drink.getFullprice()));
            drinksList.add(map);
        }

        ListAdapter adapter = new SimpleAdapter(this, drinksList, R.layout.row_drink, new String[]{"name", "price"}, new int[]{R.id.name, R.id.price});

        setListAdapter(adapter);
    }
}
