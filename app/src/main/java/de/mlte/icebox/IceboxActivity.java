package de.mlte.icebox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mlte.icebox.model.Drink;
import de.mlte.icebox.model.User;

public class IceboxActivity extends AppCompatActivity {
    public static final String DEFAULT_BASE_URL = "http://icebox.nobreakspace.org:8081";
    public static final String HDR_USER_AGENT = "Icebox Android Client";

    public static final String DRINK_MESSAGE = "de.mlte.icebox.DRINK_MESSAGE";
    private static final int USER_REQUEST = 1;
    public static final String USER_MESSAGE = "de.mlte.icebox.USER_MESSAGE";
    private static final String SETTINGS_USERNAME = "de.mlte.icebox.SETTINGS_USERNAME";
    private Webb webb;
    private User user;

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    private void refresh() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        if (networkInfo != null && networkInfo.isConnected()) {
            swipeRefreshLayout.setRefreshing(true);

            new DrinkTask().execute();
            if (user != null) {
                new UserTask().execute(user.getUsername());
            }
            return;
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private Drink[] drinks = new Drink[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icebox);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            setTitle(getResources().getText(R.string.app_name) + " " + version);
        } catch (PackageManager.NameNotFoundException e) {
            // just do nothing
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (IceboxActivity.this.drinks.length > position) {
                    Drink drink = IceboxActivity.this.drinks[position];
                    drink(drink);
                }
            }
        });

        SwipeRefreshLayout swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refresh();
                }
            }
        );


        // create the client (one-time, can be used from different threads)
        webb = Webb.create();
        webb.setDefaultHeader(Webb.HDR_USER_AGENT, HDR_USER_AGENT);

        // Restore preferences
        setUser(null);
        String username = getSharedPreferences(SettingsActivity.SETTINGS_NAME, SettingsActivity.SETTINGS_MODE).getString(SETTINGS_USERNAME, "");
        if (!username.equals("")) {
            new UserTask().execute(username);
        }
    }

    static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private class UserTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
            try {
                webb.setBaseUri(getSharedPreferences(SettingsActivity.SETTINGS_NAME, SettingsActivity.SETTINGS_MODE).getString(SettingsActivity.SETTINGS_BASE_URL, IceboxActivity.DEFAULT_BASE_URL));

                String response = webb.get("/consumers/" + urlEncode(params[0]))
                        .ensureSuccess()
                        .asString()
                        .getBody();
                Gson gson = new Gson();
                return gson.fromJson(response, User.class);
            } catch(JsonSyntaxException e) {
                return null;
            } catch (WebbException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                setUser(user);
            }
        }
    }

    private void drink(Drink drink) {
        Intent intent = new Intent(IceboxActivity.this, DrinkActivity.class);
        intent.putExtra(DRINK_MESSAGE, drink);
        intent.putExtra(USER_MESSAGE, user);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_icebox, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh_drinks:
                refresh();
                break;

            case R.id.action_select_user:
                selectUser();
                break;

            case R.id.action_settings:
                showSettings();
                break;

            case R.id.action_scan:
                scanBarcode();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        Intent intent = new Intent(IceboxActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void scanBarcode(View view) {
        scanBarcode();
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

    private class DrinkTask extends AsyncTask<Void, Void, Drink[]> {
        @Override
        protected Drink[] doInBackground(Void... params) {
            JSONArray drinks;
            try {
                webb.setBaseUri(getSharedPreferences(SettingsActivity.SETTINGS_NAME, SettingsActivity.SETTINGS_MODE).getString(SettingsActivity.SETTINGS_BASE_URL, IceboxActivity.DEFAULT_BASE_URL));
                String response = webb.get("/drinks")
                        .ensureSuccess()
                        .asString()
                        .getBody();
                final Gson gson = new Gson();
                return gson.fromJson(response, Drink[].class);
            } catch (JsonSyntaxException e) {
                return new Drink[]{};
            } catch (WebbException e) {
                return new Drink[]{};
            }
        }

        @Override
        protected void onPostExecute(Drink[] drinks) {
            IceboxActivity.this.drinks = drinks;

            List<Map<String, String>> drinksList = new ArrayList<>(drinks.length);
            for (Drink drink: drinks) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("name", drink.getName());
                map.put("price", String.format("EUR %.2f", drink.getFullprice() / 100d));
                drinksList.add(map);
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(IceboxActivity.this, drinksList, R.layout.row_drink, new String[]{"name", "price"}, new int[]{R.id.name, R.id.price});

            ListView listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(simpleAdapter);

            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setRefreshing(false);

            if (drinks.length == 0) {
                Snackbar snackbar = Snackbar
                        .make(findViewById(android.R.id.content), "Icebox service not found!", Snackbar.LENGTH_LONG);

                // Changing message text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();
            }
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

    private void selectUser() {
        Intent intent = new Intent(IceboxActivity.this, UsersActivity.class);
        startActivityForResult(intent, USER_REQUEST);
    }

    public void selectUser(View view) {
        selectUser();
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences.Editor editor = getSharedPreferences(SettingsActivity.SETTINGS_NAME, SettingsActivity.SETTINGS_MODE).edit();
        if (user != null) {
            editor.putString(SETTINGS_USERNAME, user.getUsername());
        } else {
            editor.putString(SETTINGS_USERNAME, "");
        }
        editor.commit();
    }
}
