package de.mlte.icebox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mlte.icebox.model.Drink;
import de.mlte.icebox.model.Serializer;
import de.mlte.icebox.model.User;

public class IceboxActivity extends AppCompatActivity {
    public static final String DRINK_MESSAGE = "de.mlte.icebox.DRINK_MESSAGE";
    //public static final String BASE_URI = "http://icebox.nobreakspace.org:8081";
    public static final String BASE_URI = "http://192.168.0.35:8081";
    public static final String HDR_USER_AGENT = "Icebox Android Client";
    private static final int USER_REQUEST = 1;
    public static final String USER_MESSAGE = "de.mlte.icebox.USER_MESSAGE";
    Webb webb;
    private User user;

    @Override
    protected void onResume() {
        super.onResume();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DrinkTask().execute();
            if (user != null) {
                new UserTask().execute(user.getUsername());
            }
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

        // Restore preferences
        SharedPreferences settings = getPreferences(0);
        String username = settings.getString("username", "");
        setUser(null);
        if (!username.equals("")) {
            new UserTask().execute(username);
        }
    }

    private class UserTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
            JSONObject user = webb.get("/consumers/" + params[0])
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();

            try {
                return Serializer.deserializeUser(user);
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            setUser(user);
        }
    }

    private void drink(Drink drink) {
        if (this.user != null) {
            Intent intent = new Intent(IceboxActivity.this, DrinkActivity.class);
            intent.putExtra(DRINK_MESSAGE, drink);
            intent.putExtra(USER_MESSAGE, user);
            startActivity(intent);
        }
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
                scanBarcode(null);
        }

        return super.onOptionsItemSelected(item);
    }

    public void scanBarcode(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    private void setUser(User user) {
        this.user = user;
        TextView userText = (TextView) findViewById(R.id.userText);
        if (user == null) {
            userText.setText("No User Selected");
            userText.setTextColor(Color.RED);
        } else {
            userText.setText(user.getUsername() + String.format(" (EUR %.2f)", user.getLedger() / 100d));
            userText.setTextColor(Color.GREEN);
        }
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
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                handleScanResult(scanResult.getContents());
            }
        } else if (requestCode == USER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                setUser((User) intent.getParcelableExtra(UsersActivity.RESULT_MESSAGE));
            }
        }
    }

    private void handleScanResult(String scannedBarcode) {
        if (scannedBarcode == null) return;

        for (Drink drink: drinks) {
            if (drink.getBarcode().equals(scannedBarcode)) {
                drink(drink);
                return;
            }
        }

        // Drink not found
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AlertDialogFragment.ARG_TITLE, "Barcode not found!");
        bundle.putString(AlertDialogFragment.ARG_MESSAGE, "Barcode " + scannedBarcode + " not found in the Icebox database!");
        alertDialogFragment.setArguments(bundle);
        alertDialogFragment.show(getFragmentManager(), "tag");
    }

    public void selectUser(View view) {
        Intent intent = new Intent(IceboxActivity.this, UsersActivity.class);
        startActivityForResult(intent, USER_REQUEST);
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        if (user != null) {
            editor.putString("username", user.getUsername());
        } else {
            editor.putString("username", "");
        }
        editor.commit();
    }
}
