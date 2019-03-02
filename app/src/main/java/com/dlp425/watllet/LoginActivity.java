package com.dlp425.watllet;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Spinner spinner = findViewById(R.id.res_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.res_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String checkBoxText = "I agree to all the <a href='https://www.youtube.com/watch?v=dQw4w9WgXcQ' > Terms and Conditions</a>";
        CheckBox tosCheck = findViewById(R.id.checkbox_tos);
        tosCheck.setText(Html.fromHtml(checkBoxText, Html.FROM_HTML_MODE_LEGACY));
        tosCheck.setMovementMethod(LinkMovementMethod.getInstance());


        final SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);

        final ProgressBar progress = findViewById(R.id.progressBar);

        final EditText accountText = findViewById(R.id.input_account);
        final EditText pinText = findViewById(R.id.input_password);
        final Spinner resSpinner = findViewById(R.id.res_spinner);
        final CheckBox weekendCheck = findViewById(R.id.weekends);

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
                String res = resSpinner.getSelectedItem().toString();

                SharedPreferences.Editor edit = sp1.edit();
                edit.putString("Acc", acc.trim());
                edit.putString("Pin", pin.trim());
                edit.putString("Res", res.trim());
                edit.putBoolean("Weekend", weekendCheck.isChecked());
                edit.apply();


                final WebView webview = findViewById(R.id.login_webview);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.getSettings().setDomStorageEnabled(true);
                webview.getSettings().setAppCacheEnabled(false);
                webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

                check_login();
            }
        });

        CheckBox autodate = findViewById(R.id.autoDate);
        autodate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    SharedPreferences.Editor edit = sp1.edit();
                    edit.remove("LastDay");
                    edit.apply();
                }else{
                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(getFragmentManager(),"Last Day");
                }
            }
        });

    }

    void check_login() {

        final WebView webview = findViewById(R.id.login_webview);
        final ProgressBar progress = findViewById(R.id.progressBar);
        final Button loginButton = findViewById(R.id.btn_login);

        final SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);

        final String acc = sp1.getString("Acc", null);
        final String pin = sp1.getString("Pin", null);

        loginButton.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {

                Toast.makeText(getApplicationContext(),
                        "WebView Error" + error.getDescription(),
                        Toast.LENGTH_SHORT).show();

                super.onReceivedError(view, request, error);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Account/LogOn#first")) {
                    webview.loadUrl("javascript:(function(){" +
                            String.format("document.getElementById('Account').value='%s';", acc) +
                            String.format("document.getElementById('Password').value='%s';", pin) +
                            "document.forms[1].submit();" +
                            "})()");
                } else if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Account/LogOn")) {
                    loginButton.setEnabled(true);
                    progress.setVisibility(View.INVISIBLE);

                    SharedPreferences.Editor edit = sp1.edit();
                    edit.clear();
                    edit.apply();

                    Toast.makeText(getBaseContext(), "Wrong Account or PIN!", Toast.LENGTH_LONG).show();

                }else if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Account/Personal")) {
                    Toast.makeText(getBaseContext(), "Logged In!", Toast.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                    progress.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }

        }});
        webview.loadUrl("https://watcard.uwaterloo.ca/OneWeb/Account/LogOn#first");
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

        CheckBox tos = findViewById(R.id.checkbox_tos);


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

        if(!tos.isChecked()){
            tos.setError("");
            valid = false;
        }else{
            tos.setError(null);
        }

        return valid;
    }

}
