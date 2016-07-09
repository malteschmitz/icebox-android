package de.mlte.icebox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import de.mlte.icebox.model.Drink;

public class DrinkActivity extends AppCompatActivity {

    private Drink drink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        this.drink = (Drink) intent.getParcelableExtra(IceboxActivity.DRINK_MESSAGE);

        TextView nameTextView = (TextView) findViewById(R.id.nameText);
        nameTextView.setText(drink.getName());

        TextView barcodeTextView = (TextView) findViewById(R.id.barcodeText);
        barcodeTextView.setText(drink.getBarcode());
    }

}
