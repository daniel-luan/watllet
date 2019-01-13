package com.dlp425.watllet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);


        final ProgressBar progress = findViewById(R.id.progressBar);

        final EditText accountText = findViewById(R.id.input_account);
        final EditText pinText = findViewById(R.id.input_password);

        final Button loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validate()) {
                    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                    return;
                }

                loginButton.setEnabled(false);
                progress.setVisibility(View.VISIBLE);

                String acc = accountText.getText().toString();
                String pin = pinText.getText().toString();

                SharedPreferences.Editor edit = sp1.edit();
                edit.putString("Acc", acc.trim());
                edit.putString("Pin", pin.trim());
                edit.apply();

//                loginButton.setEnabled(true);
//                progress.setVisibility(View.INVISIBLE);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }


    public boolean validate() {
        boolean valid = true;

        EditText accountText = findViewById(R.id.input_account);
        String acc = accountText.getText().toString();

        EditText pinText = findViewById(R.id.input_password);
        String pin = pinText.getText().toString();

        if (acc.isEmpty() || acc.length() != 8) {
            accountText.setError("Enter a valid Watcard Number");
            valid = false;
        } else {
            accountText.setError(null);
        }

        if (pin.isEmpty()) {
            pinText.setError("Enter a Pin");
            valid = false;
        } else {
            accountText.setError(null);
        }

        return valid;
    }

}
