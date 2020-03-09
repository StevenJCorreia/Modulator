package com.stevenjcorreia.modulator.activities;

import com.stevenjcorreia.modulator.R;
import com.stevenjcorreia.modulator.utils.Modem;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

public class ModemActivity extends AppCompatActivity {
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText originalTextValue = findViewById(R.id.originalTextValue);
        final TextView resultTextValue = findViewById(R.id.resultTextValue);
        Button modulateButton = findViewById(R.id.modulateButton);
        final Switch modeValue = findViewById(R.id.modeValue);

        modeValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                modeValue.setText(isChecked ? getResources().getString(R.string.manchester) : getResources().getString(R.string.nrz_i));

                originalTextValue.setText("");
                resultTextValue.setText("");
            }
        });

        modulateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set TextView to result value
                resultTextValue.setText(Modem.execute(originalTextValue.getText().toString(), modeValue.isChecked() ? Modem.MANCHESTER_IEEE : Modem.NRZ_I));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra("url", id == R.id.aboutApp ? getResources().getString(R.string.repo_link) : getResources().getString(R.string.linkedin_profile));
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }
}
