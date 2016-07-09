package de.mlte.icebox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;

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
    public static final String DRINK_MESSAGE = "de.mlte.icebox.DRINK_MESSAGE";
    //public static final String BASE_URI = "http://icebox.nobreakspace.org:8081";
    public static final String BASE_URI = "http://172.23.208.176:8081";
    public static final String HDR_USER_AGENT = "Icebox Android Client";
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
                    drink(drink);
                }
            }
        });

        // create the client (one-time, can be used from different threads)
        webb = Webb.create();
        webb.setBaseUri(BASE_URI);
        webb.setDefaultHeader(Webb.HDR_USER_AGENT, HDR_USER_AGENT);
    }

    private void drink(Drink drink) {
        Intent intent = new Intent(IceboxActivity.this, DrinkActivity.class);
        intent.putExtra(DRINK_MESSAGE, drink);
        startActivity(intent);
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
            final String scannedBarcode = scanResult.getContents();
            if (scannedBarcode != null) {
                for (Drink drink: drinks) {
                    if (drink.getBarcode().equals(scannedBarcode)) {
                        drink(drink);
                        return;
                    }
                }

                // Drink not found
                new AlertDialog.Builder(this)
                        .setTitle("Barcode not found!")
                        .setMessage("Barcode " + scannedBarcode + " not found in the Icebox database!")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
}
