package com.dlp425.watllet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        Typeface font = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf");

        TextView stop = findViewById(R.id.stop);
        stop.setTypeface(font);

        loadCache();

        SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);

        if (sp1.getString("Acc", null) == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            final WebView webview = findViewById(R.id.webview);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.getSettings().setDomStorageEnabled(true);
            webview.getSettings().setAppCacheEnabled(false);
            webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            getBalance();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_logout:
                SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);
                SharedPreferences.Editor edit = sp1.edit();
                edit.clear();
                edit.apply();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void loadCache() {
        SharedPreferences cache = this.getSharedPreferences("Cache", MODE_PRIVATE);


        TextView nameText = findViewById(R.id.name);
        nameText.setText(String.format(Locale.getDefault(), "Hello %s", cache.getString("Name", "")));

        TextView numberText = findViewById(R.id.cardNum);
        numberText.setText(String.format(Locale.getDefault(), "Card Number: %d", cache.getInt("Number", 0)));

        TextView mealText = findViewById(R.id.meal);
        mealText.setText(String.format(Locale.getDefault(), "Meal Plan: $%.2f", cache.getFloat("Meal", 0)));

        TextView flexText = findViewById(R.id.flex);
        flexText.setText(String.format(Locale.getDefault(), "Flex: $%.2f", cache.getFloat("Flex", 0)));

        TextView usageView = findViewById(R.id.usage);
        usageView.setText(String.format(Locale.getDefault(), "Usage today: $%.2f (%s)", cache.getFloat("Usage", 0), cache.getString("Today", "")));


        TextView lastDayText = findViewById(R.id.lastDay);
        TextView targetText = findViewById(R.id.target);
        lastDayText.setText(String.format(Locale.getDefault(), "Last Day: %s (%d days away)", cache.getString("LastDay", ""), cache.getInt("Diff", 0)));
        targetText.setText(String.format(Locale.getDefault(), "Target Today: $%.2f", cache.getFloat("Target", 0)));

    }

    public void buttonRefresh(View view) {
        getBalance();
    }

    public void stop(View v) {

        final WebView webview = findViewById(R.id.webview);
        final ProgressBar progress = findViewById(R.id.progress);
        final Button button = findViewById(R.id.refresh);
        webview.stopLoading();
        webview.getSettings().setJavaScriptEnabled(false);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("about:blank");
        progress.setVisibility(View.INVISIBLE);
        button.setEnabled(true);
    }

    void getBalance() {

        final double totalSteps = 6.0;
        final WebView webview = findViewById(R.id.webview);
        final ProgressBar progress = findViewById(R.id.progress);
        final Button button = findViewById(R.id.refresh);

        button.setEnabled(false);

        progress.setVisibility(View.VISIBLE);
        progress.setProgress(1);

        SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);
        final SharedPreferences cache = this.getSharedPreferences("Cache", MODE_PRIVATE);
        final SharedPreferences.Editor cache_edit = cache.edit();


        final String acc = sp1.getString("Acc", null);
        final String pin = sp1.getString("Pin", null);
        final String res = sp1.getString("Res", null);

        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().getHost().contains("google") ||
                        request.getUrl().getHost().contains("twitter")) {
                    return true;
                }

                return false;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Account/LogOn")) {
                    try {
                        webview.loadUrl("javascript:(function(){" +
                                String.format("document.getElementById('Account').value='%s';", acc) +
                                String.format("document.getElementById('Password').value='%s';", pin) +
                                "document.forms[1].submit();" +
                                "})()");
                        progress.setProgress((int) (1 / totalSteps * 100), true);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Error logging in!", Toast.LENGTH_SHORT).show();
                        stop(null);
                    }
                } else if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Account/Personal")) {
                    try {
                        webview.evaluateJavascript("document.getElementsByClassName('ow-value')[0].innerHTML",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        TextView textView = findViewById(R.id.name);
                                        String name = value.replace("\"", "");
                                        cache_edit.putString("Name", name);
                                        cache_edit.apply();
                                        textView.setText(String.format(Locale.getDefault(), "Hello %s", name));
                                        // textView.setText("Hello Mr. Goose");
                                    }
                                });
                        webview.evaluateJavascript("document.getElementsByClassName('ow-value')[1].innerHTML",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {

                                        int number = Integer.parseInt(value.replace("\"", "").replace(" ", ""));
                                        cache_edit.putInt("Number", number);
                                        cache_edit.apply();
                                        TextView textView = findViewById(R.id.cardNum);
                                        textView.setText(String.format(Locale.getDefault(), "Card Number: %d", number));
                                        // textView.setText("Card Number: 12345678");
                                    }
                                });
                        webview.loadUrl("https://watcard.uwaterloo.ca/OneWeb/Financial/Balances");
                        progress.setProgress((int) (2 / totalSteps * 100), true);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Error getting name and card number!", Toast.LENGTH_SHORT).show();
                        stop(null);
                    }
                } else if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Financial/Balances")) {
                    try {
                        webview.evaluateJavascript(
                                "(function() {for (let tr of document.querySelectorAll('tr')) " +
                                        "{if (tr.cells[0].innerHTML == '1') " +
                                        "{return tr.cells[3].innerHTML}}})();",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        Double meal = Double.parseDouble(value.replaceAll("[^\\d.]", ""));
                                        cache_edit.putFloat("Meal", meal.floatValue());
                                        cache_edit.apply();
                                        TextView textView = findViewById(R.id.meal);
                                        textView.setText(String.format(Locale.getDefault(), "Meal Plan: $%.2f", meal));
                                    }
                                });
                        webview.evaluateJavascript(
                                "(function() {for (let tr of document.querySelectorAll('tr')) " +
                                        "{if (tr.cells[0].innerHTML == '6') " +
                                        "{return tr.cells[3].innerHTML}}})();",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        Double flex = Double.parseDouble(value.replaceAll("[^\\d.]", ""));
                                        cache_edit.putFloat("Flex", flex.floatValue());
                                        cache_edit.apply();
                                        TextView textView = findViewById(R.id.flex);
                                        textView.setText(String.format(Locale.getDefault(), "Flex: $%.2f", flex));
                                    }
                                });


                        Date today = Calendar.getInstance().getTime();
                        String reportDate = df.format(today).replace("/", "%2F");


                        webview.loadUrl("https://watcard.uwaterloo.ca/OneWeb/Financial/TransactionsPass?dateFrom="
                                + reportDate + "+00%3A00%3A00&dateTo=" + reportDate + "+23%3A59%3A59&returnRows=1000&_=1545508546483");
                        progress.setProgress((int) (3 / totalSteps * 100), true);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Error getting balance!", Toast.LENGTH_SHORT).show();
                        stop(null);
                    }
                } else if (url.startsWith("https://watcard.uwaterloo.ca/OneWeb/Financial/TransactionsPass?")) {
                    try {
                        webview.evaluateJavascript(
                                "(function(){var amount = 0;for (let tr of document.querySelectorAll('tr')) " +
                                        "{if ((tr.cells[1].getAttribute('data-title') == 'Amount:') && (tr.cells[2].innerHTML == '1'))" +
                                        "{amount = amount + parseFloat(tr.cells[1].innerHTML.split('$')[1])}}" +
                                        "return amount.toFixed(2);})();",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        Date today = Calendar.getInstance().getTime();
                                        String reportDate = df.format(today);

                                        Float usage = Float.parseFloat(value.replaceAll("[^\\d.]", ""));

                                        cache_edit.putString("Today", reportDate);
                                        cache_edit.putFloat("Usage", usage);
                                        cache_edit.apply();

                                        TextView textView = findViewById(R.id.usage);
                                        textView.setText(String.format(Locale.getDefault(), "Usage today: $%.2f (%s)", usage, reportDate));
                                    }
                                });
                        webview.loadUrl("https://uwaterloo.ca/registrar/important-dates/entry?id=42");
                        progress.setProgress((int) (4 / totalSteps * 100), true);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Error getting usage!", Toast.LENGTH_SHORT).show();
                        stop(null);
                    }
                } else if (url.equals("https://uwaterloo.ca/registrar/important-dates/entry?id=42")) {
                    try {
                        webview.evaluateJavascript(
                                "(function() " +
                                        "{for (let entry of document.getElementsByClassName('important-dates__dates')[0].children)" +
                                        "{ var date = Date.parse(entry.innerText.split('\\n')[1]); " +
                                        "if (date > Date.now()){ return date;}}})();",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        TextView lastDayText = findViewById(R.id.lastDay);
                                        TextView targetText = findViewById(R.id.target);
                                        TextView mealText = findViewById(R.id.meal);

                                        Date lastDay = new java.util.Date(Long.parseLong(value.replace(".0", "")));
                                        String lastDayString = df.format(lastDay);
                                        cache_edit.putString("LastDay", lastDayString);


                                        Date now = new java.util.Date();
                                        int diff = Math.toIntExact(TimeUnit.MILLISECONDS.toDays(lastDay.getTime() - now.getTime()));
                                        float meal = cache.getFloat("Meal", Float.parseFloat(mealText.getText().toString().replace("Meal Plan: $", "")));
                                        float target = meal / diff;
                                        cache_edit.putInt("Diff", diff);
                                        cache_edit.putFloat("Target", target);
                                        cache_edit.apply();

                                        lastDayText.setText(String.format(Locale.getDefault(), "Last Day: %s (%d days away)", lastDayString, diff));
                                        targetText.setText(String.format(Locale.getDefault(), "Target Today: $%.2f", target));

                                    }
                                });
                        progress.setProgress((int) (5 / totalSteps * 100), true);
                        webview.loadUrl("https://uwaterloo.ca/food-services/menu");
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Error getting target!", Toast.LENGTH_SHORT).show();
                        stop(null);
                    }
                } else if (url.equals("https://uwaterloo.ca/food-services/menu")) {
                    int resNum = 1;

                    switch (res) {
                        case "V1":
                            resNum = 1;
                            break;
                        case "REV":
                            resNum = 2;
                            break;
                        case "UWP":
                            resNum = 3;
                            break;
                        default:
                            resNum = 1;
                            break;
                    }

                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                    if ((day > 5) || (day < 1)) day = 0;

                    webview.loadUrl(String.format(Locale.getDefault(), "javascript:(function() {document.getElementsByClassName('uw_food_services-menu')[0].rows[%d].cells[%d].scrollIntoView()})()", resNum, day));
                    progress.setProgress((int) (6 / totalSteps * 100), true);
                    progress.setVisibility(View.INVISIBLE);

                    button.setEnabled(true);
                }
            }
        });

        webview.loadUrl("https://watcard.uwaterloo.ca/OneWeb/Account/LogOn");
    }


}
