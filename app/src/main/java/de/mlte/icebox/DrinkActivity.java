package de.mlte.icebox;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.goebl.david.Webb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mlte.icebox.model.Consumption;
import de.mlte.icebox.model.Drink;
import de.mlte.icebox.model.Serializer;
import de.mlte.icebox.model.User;

public class DrinkActivity extends AppCompatActivity {
    private Drink drink;
    Webb webb;
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
        webb.setBaseUri(IceboxActivity.BASE_URI);
        webb.setDefaultHeader(Webb.HDR_USER_AGENT, IceboxActivity.HDR_USER_AGENT);
    }

    public void buy(View view) {
        Consumption consumption = new Consumption() {{
            barcode = drink.getBarcode();
            username = DrinkActivity.this.user.getUsername();
        }};
        new BuyTask().execute(consumption);
        Button button = (Button) findViewById(R.id.buy);
        button.setEnabled(false);
    }

    private class BuyTask extends AsyncTask<Consumption, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Consumption... params) {
            JSONObject body = null;
            try {
                body = Serializer.serializeConsumption(params[0]);
                Log.d("mlte", body.toString());
                final JSONObject response = webb.post("/consumptions")
                        .body(body)
                        .ensureSuccess()
                        .asJsonObject()
                        .getBody();
                Log.d("mlte", response.toString());
            } catch (JSONException e) {
                // TODO handle serialize excpetion
            }

            // TODO handle possible exceptions thrown by ensureSuccess

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            DrinkActivity.this.finish();
        }
    }

}
