package com.stevenjcorreia.modulator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.stevenjcorreia.modulator.Utils.Modem;

public class MainActivity extends AppCompatActivity {
    Context context = this;

    EditText originalTextValue;
    TextView resultTextValue;
    Button modulateButton;
    Switch modeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalTextValue = findViewById(R.id.originalTextValue);
        resultTextValue = findViewById(R.id.resultTextValue);
        modulateButton = findViewById(R.id.modulateButton);
        modeValue = findViewById(R.id.modeValue);

        modeValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                modeValue.setText(isChecked ? "Manchester (IEEE)" : "NRZ-I");
                originalTextValue.setText("");
                resultTextValue.setText("");
            }
        });

        modulateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!resultTextValue.getText().toString().isEmpty())
                    resultTextValue.setText("");

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Set textview to result value
                        resultTextValue.setText(Modem.execute(originalTextValue.getText().toString(), modeValue.isChecked() ? Modem.MANCHESTER_IEEE : Modem.NRZ_I));
                    }
                });
                thread.start();
            }
        });
    }
}
