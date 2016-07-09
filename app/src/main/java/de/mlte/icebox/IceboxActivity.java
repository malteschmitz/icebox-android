package de.mlte.icebox;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.goebl.david.Webb;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mlte.icebox.model.Drink;
import de.mlte.icebox.model.Serializer;

public class IceboxActivity extends AppCompatActivity {
    public static final String DRINKS_MESSAGE = "de.mlte.icebox.DRINKS_MESSAGE";
    Webb webb;

    @Override
    protected void onResume() {
        super.onResume();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DrinkTask().execute();
        }
    }

    private List<Drink> drinks = Collections.EMPTY_LIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icebox);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (IceboxActivity.this.drinks.size() > position) {
                    Drink drink = IceboxActivity.this.drinks.get(position);
                    Log.d("mlte", drink.getName() + " clicked");
                }
            }
        });

        // create the client (one-time, can be used from different threads)
        webb = Webb.create();
        webb.setBaseUri("http://icebox.nobreakspace.org:8081");
        webb.setDefaultHeader(Webb.HDR_USER_AGENT, "Icebox Android Client");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Log.d("mlte", "Settings icon clicked");
                break;

            case R.id.action_scan:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
        }

        return super.onOptionsItemSelected(item);
    }

    private class DrinkTask extends AsyncTask<Void, Void, List<Drink>> {
        @Override
        protected List<Drink> doInBackground(Void... params) {
            JSONArray drinks = webb.get("/drinks")
                    .ensureSuccess()
                    .asJsonArray()
                    .getBody();

            try {
                return Serializer.deserializeDrinks(drinks);
            } catch (JSONException e) {
                return Collections.EMPTY_LIST;
            }
        }

        @Override
        protected void onPostExecute(List<Drink> drinks) {
            //Intent intent = new Intent(IceboxActivity.this, DrinksActivity.class);
            //Drink[] drinkArray = drinks.toArray(new Drink[drinks.size()]);
            //intent.putExtra(DRINKS_MESSAGE, drinkArray);
            //startActivity(intent);

            IceboxActivity.this.drinks = drinks;

            List<Map<String, String>> drinksList = new ArrayList<>(drinks.size());
            for (Drink drink: drinks) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("name", drink.getName());
                map.put("price", String.format("EUR %.2f", drink.getFullprice() / 100d));
                drinksList.add(map);
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(IceboxActivity.this, drinksList, R.layout.row_drink, new String[]{"name", "price"}, new int[]{R.id.name, R.id.price});

            ListView listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(simpleAdapter);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String re = scanResult.getContents();
            Log.d("mlte", "Scan: " + re);
        }
    }
}
