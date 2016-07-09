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
import android.widget.EditText;

import com.goebl.david.Webb;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collections;
import java.util.List;

import de.mlte.icebox.model.Drink;
import de.mlte.icebox.model.Serializer;

public class IceboxActivity extends AppCompatActivity {
    public static final String DRINKS_MESSAGE = "de.mlte.icebox.DRINKS_MESSAGE";
    Webb webb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icebox);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getDrinks(View view) throws JSONException {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DrinkTask().execute();
        } else {
            //TODO: display error
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
            Intent intent = new Intent(IceboxActivity.this, DrinksActivity.class);
            Drink[] drinkArray = drinks.toArray(new Drink[drinks.size()]);
            intent.putExtra(DRINKS_MESSAGE, drinkArray);
            startActivity(intent);
        }
    }

    public void scan(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String re = scanResult.getContents();
            EditText edit = (EditText) findViewById(R.id.edit_message);
            edit.setText(re);
        }
    }
}
