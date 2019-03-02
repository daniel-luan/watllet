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
import java.text.ParseException;
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

                SharedPreferences sp2 = this.getSharedPreferences("Cache", MODE_PRIVATE);
                SharedPreferences.Editor edit2 = sp2.edit();
                edit2.clear();
                edit2.apply();

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
        nameText.setText(String.format(Locale.getDefault(),
                "Hello %s", cache.getString("Name", "")));

        TextView numberText = findViewById(R.id.cardNum);
        numberText.setText(String.format(Locale.getDefault(),
                "Card Number: %d", cache.getInt("Number", 0)));

        TextView mealText = findViewById(R.id.meal);
        mealText.setText(String.format(Locale.getDefault(),
                "Meal Plan: $%.2f", cache.getFloat("Meal", 0)));

        TextView flexText = findViewById(R.id.flex);
        flexText.setText(String.format(Locale.getDefault(),
                "Flex: $%.2f", cache.getFloat("Flex", 0)));

        TextView usageView = findViewById(R.id.usage);
        usageView.setText(String.format(Locale.getDefault(),
                "Usage today: $%.2f (%s)",
                cache.getFloat("Usage", 0),
                cache.getString("Today", "")));


        TextView lastDayText = findViewById(R.id.lastDay);
        TextView targetText = findViewById(R.id.target);
        int readingWeek = cache.getInt("ReadingWeek", 0);
        if (readingWeek == 0) {
            lastDayText.setText(String.format(Locale.getDefault(),
                    "Last Day: %s (%d days away)",
                    cache.getString("LastDay", ""),
                    cache.getInt("Diff", 0)));
        } else {
            lastDayText.setText(String.format(Locale.getDefault(),
                    "Last Day: %s (%d days away) - %d Days",
                    cache.getString("LastDay", ""),
                    cache.getInt("Diff", 0), readingWeek));
        }
        targetText.setText(String.format(Locale.getDefault(),
                "Target Today: $%.2f",
                cache.getFloat("Target", 0)));

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

    static long days(Date start, Date end) {
        //Ignore argument check

        Calendar c1 = Calendar.getInstance();
        c1.setTime(start);
        int w1 = c1.get(Calendar.DAY_OF_WEEK);
        c1.add(Calendar.DAY_OF_WEEK, -w1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(end);
        int w2 = c2.get(Calendar.DAY_OF_WEEK);
        c2.add(Calendar.DAY_OF_WEEK, -w2);

        //end Saturday to start Saturday
        long days = (c2.getTimeInMillis() - c1.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        long daysWithoutWeekendDays = days - (days * 2 / 7);

        // Adjust days to add on (w2) and days to subtract (w1) so that Saturday
        // and Sunday are not included
        if (w1 == Calendar.SUNDAY && w2 != Calendar.SATURDAY) {
            w1 = Calendar.MONDAY;
        } else if (w1 == Calendar.SATURDAY && w2 != Calendar.SUNDAY) {
            w1 = Calendar.FRIDAY;
        }

        if (w2 == Calendar.SUNDAY) {
            w2 = Calendar.MONDAY;
        } else if (w2 == Calendar.SATURDAY) {
            w2 = Calendar.FRIDAY;
        }

        return daysWithoutWeekendDays - w1 + w2;
    }

    void getBalance() {

        final double totalSteps = 7.0;
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
        final String userLastDay = sp1.getString("LastDay", null);
        final boolean weekends = sp1.getBoolean("Weekend", true);

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
                return request.getUrl().getHost().contains("google") ||
                        request.getUrl().getHost().contains("twitter");

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

                                        float usage = Float.parseFloat(value.replaceAll("[^\\d.]", ""));

                                        cache_edit.putString("Today", reportDate);
                                        cache_edit.putFloat("Usage", usage);
                                        cache_edit.apply();

                                        TextView textView = findViewById(R.id.usage);
                                        textView.setText(String.format(Locale.getDefault(), "Usage today: $%.2f (%s)", usage, reportDate));
                                    }
                                });
                        webview.loadUrl("https://uwaterloo.ca/registrar/important-dates/entry?id=6");
                        progress.setProgress((int) (4 / totalSteps * 100), true);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Error getting usage!", Toast.LENGTH_SHORT).show();
                        stop(null);
                    }
                } else if (url.equals("https://uwaterloo.ca/registrar/important-dates/entry?id=6")) {
                    webview.evaluateJavascript("(function() {" +
                            "for (let entry of document.getElementsByClassName('important-dates__dates')[0].children){" +
                            "if (entry.innerText === '') continue;" +
                            "var dates = entry.innerText.split('\\n')[1].split(' TO ');" +
                            "var start = Date.parse(dates[0]);" +
                            "var end = Date.parse(dates[1]);" +
                            "if (Date.now() > end) continue;" +
                            "if (Date.now() > start) return  Math.floor((end - Date.now()) / 86400000) + 1;" +
                            "else return Math.floor((end - start) / 86400000) + 1;}" +
                            "return 0;" +
                            "})()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            cache_edit.putInt("ReadingWeek", Integer.parseInt(value));
                            progress.setProgress((int) (5 / totalSteps * 100), true);
                            webview.loadUrl("https://uwaterloo.ca/registrar/important-dates/entry?id=42");
                        }
                    });
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
                                        if(userLastDay != null){
                                            try {
                                                lastDay = new SimpleDateFormat("dd/MM/yyyy").parse(userLastDay);
                                            }catch (ParseException e){

                                            }
                                        }

                                        String lastDayString = df.format(lastDay);
                                        cache_edit.putString("LastDay", lastDayString);


                                        Date now = new java.util.Date();

                                        int diff;

                                        if (weekends) {
                                            diff = Math.toIntExact(TimeUnit.MILLISECONDS.toDays(lastDay.getTime() - now.getTime()));
                                        } else {
                                            diff = Math.toIntExact(days(now, lastDay));
                                        }
                                        int readingWeek = cache.getInt("ReadingWeek", 0);

                                        diff = diff - readingWeek;

                                        float meal = cache.getFloat("Meal", Float.parseFloat(mealText.getText().toString().replace("Meal Plan: $", "")));
                                        float target = meal / diff;
                                        cache_edit.putInt("Diff", diff);
                                        cache_edit.putFloat("Target", target);
                                        cache_edit.apply();
                                        if (readingWeek == 0) {
                                            lastDayText.setText(String.format(Locale.getDefault(), "Last Day: %s (%d days away)", lastDayString, diff));
                                        } else {
                                            lastDayText.setText(String.format(Locale.getDefault(), "Last Day: %s (%d days away) - %d Days", lastDayString, diff, readingWeek));
                                        }
                                        targetText.setText(String.format(Locale.getDefault(), "Target Today: $%.2f", target));

                                    }
                                });
                        progress.setProgress((int) (6 / totalSteps * 100), true);
                        webview.loadUrl("https://uwaterloo.ca/food-services/menu");
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), "Error getting target!", Toast.LENGTH_SHORT).show();
                        stop(null);
                    }
                } else if (url.equals("https://uwaterloo.ca/food-services/menu")) {
                    int resNum;

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
                    progress.setProgress((int) (7 / totalSteps * 100), true);
                    progress.setVisibility(View.INVISIBLE);

                    button.setEnabled(true);
                }
            }
        });

        webview.loadUrl("https://watcard.uwaterloo.ca/OneWeb/Account/LogOn");
    }

}
