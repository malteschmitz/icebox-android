package de.mlte.icebox;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;

import org.json.JSONException;
import org.json.JSONObject;

import de.mlte.icebox.model.Consumption;
import de.mlte.icebox.model.Drink;
import de.mlte.icebox.model.Serializer;
import de.mlte.icebox.model.User;

public class DrinkActivity extends AppCompatActivity {
    private Drink drink;
    private Webb webb;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        this.drink = (Drink) intent.getParcelableExtra(IceboxActivity.DRINK_MESSAGE);
        this.user = (User) intent.getParcelableExtra(IceboxActivity.USER_MESSAGE);

        TextView nameTextView = (TextView) findViewById(R.id.nameText);
        nameTextView.setText(drink.getName());

        TextView barcodeTextView = (TextView) findViewById(R.id.barcodeText);
        barcodeTextView.setText(drink.getBarcode());

        // create the client (one-time, can be used from different threads)
        webb = Webb.create();
        webb.setDefaultHeader(Webb.HDR_USER_AGENT, IceboxActivity.HDR_USER_AGENT);
    }

    public void buy(View view) {
        if (this.user != null) {
            Consumption consumption = new Consumption() {{
                barcode = drink.getBarcode();
                username = DrinkActivity.this.user.getUsername();
            }};
            new BuyTask().execute(consumption);
            Button button = (Button) findViewById(R.id.buy);
            button.setEnabled(false);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString(AlertDialogFragment.ARG_TITLE, "Unable to buy!");
            bundle.putString(AlertDialogFragment.ARG_MESSAGE, "Please select a user first.");
            AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
            alertDialogFragment.setArguments(bundle);
            alertDialogFragment.show(getFragmentManager(), "tag");
        }
    }

    private class BuyTask extends AsyncTask<Consumption, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Consumption... params) {
            JSONObject body = null;
            try {
                body = Serializer.serializeConsumption(params[0]);
                webb.setBaseUri(getSharedPreferences(SettingsActivity.SETTINGS_NAME, SettingsActivity.SETTINGS_MODE).getString(SettingsActivity.SETTINGS_BASE_URL, IceboxActivity.DEFAULT_BASE_URL));
                JSONObject response = webb.post("/consumptions")
                        .body(body)
                        .ensureSuccess()
                        .asJsonObject()
                        .getBody();
                Log.d("mlte", "Buy Response: " + response.toString());
                // TODO handle response
            } catch (JSONException e) {
                return false;
            } catch (WebbException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Button button = (Button) findViewById(R.id.buy);
            button.setEnabled(true);
            if (success) {
                DrinkActivity.this.finish();
            } else {
                Bundle bundle = new Bundle();
                bundle.putString(AlertDialogFragment.ARG_TITLE, "Error");
                bundle.putString(AlertDialogFragment.ARG_MESSAGE, "Unable to buy the drink.");
                AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
                alertDialogFragment.setArguments(bundle);
                alertDialogFragment.show(getFragmentManager(), "tag");
            }
        }
    }

}
