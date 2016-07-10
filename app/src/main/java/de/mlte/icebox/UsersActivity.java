package de.mlte.icebox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.goebl.david.Webb;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mlte.icebox.model.Drink;
import de.mlte.icebox.model.Serializer;
import de.mlte.icebox.model.User;

public class UsersActivity extends AppCompatActivity {
    public static final String RESULT_MESSAGE = "de.mlte.icebox.RESULT_MESSAGE";
    Webb webb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.usersList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (UsersActivity.this.users.size() > position) {
                    User user = UsersActivity.this.users.get(position);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(RESULT_MESSAGE, user);
                    setResult(Activity.RESULT_OK, returnIntent);
                    UsersActivity.this.finish();
                }
            }
        });

        // create the client (one-time, can be used from different threads)
        webb = Webb.create();
        webb.setBaseUri(IceboxActivity.BASE_URI);
        webb.setDefaultHeader(Webb.HDR_USER_AGENT, IceboxActivity.HDR_USER_AGENT);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new UserTask().execute();
        }
    }

    private List<User> users = Collections.EMPTY_LIST;

    private class UserTask extends AsyncTask<Void, Void, List<User>> {
        @Override
        protected List<User> doInBackground(Void... params) {
            JSONArray users = webb.get("/consumers")
                    .ensureSuccess()
                    .asJsonArray()
                    .getBody();

            try {
                return Serializer.deserializeUsers(users);
            } catch (JSONException e) {
                return Collections.EMPTY_LIST;
            }
        }

        @Override
        protected void onPostExecute(List<User> users) {
            UsersActivity.this.users = users;

            List<Map<String, String>> usersList = new ArrayList<>(users.size());
            for (User user : users) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", user.getUsername());
                map.put("ledger", String.format("EUR %.2f", user.getLedger() / 100d));
                usersList.add(map);
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(UsersActivity.this, usersList, R.layout.row_user, new String[]{"username", "ledger"}, new int[]{R.id.username, R.id.ledger});

            ListView listView = (ListView) findViewById(R.id.usersList);
            listView.setAdapter(simpleAdapter);
        }
    }
}
