package de.mlte.icebox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    public static final String SETTINGS_BASE_URL = "de.mlte.icebox.SETTINGS_BASE_URL";
    public static final String SETTINGS_NAME = "de.mlte.icebox.SETTINGS_NAME";
    public static final int SETTINGS_MODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String url = getSharedPreferences(SettingsActivity.SETTINGS_NAME, SettingsActivity.SETTINGS_MODE).getString(SETTINGS_BASE_URL, IceboxActivity.DEFAULT_BASE_URL);
        EditText edit = (EditText) findViewById(R.id.base_url_edit);
        edit.setText(url);
    }

    public void apply(View view) {
        SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_NAME, SETTINGS_MODE).edit();
        EditText edit = (EditText) findViewById(R.id.base_url_edit);
        editor.putString(SETTINGS_BASE_URL, edit.getText().toString());
        editor.commit();
        finish();
    }

    public void nbsp(View view) {
        EditText edit = (EditText) findViewById(R.id.base_url_edit);
        edit.setText(IceboxActivity.DEFAULT_BASE_URL);
    }

}
