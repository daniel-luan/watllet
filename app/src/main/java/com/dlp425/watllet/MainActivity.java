package com.dlp425.watllet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

    }

    public void buttonRefresh(View view) {
        getBalance();
    }

    void getBalance() {

        final double totalSteps = 6.0;
        final WebView webview = findViewById(R.id.webview);
        final ProgressBar progress = findViewById(R.id.progress);

        progress.setVisibility(View.VISIBLE);
        progress.setProgress(1);

        SharedPreferences sp1 = this.getSharedPreferences("Login", MODE_PRIVATE);

        final String acc = sp1.getString("Acc", null);
        final String pin = sp1.getString("Pin", null);

        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Account/LogOn")) {
                    webview.loadUrl("javascript:(function(){" +
                            String.format("document.getElementById('Account').value='%s';", acc) +
                            String.format("document.getElementById('Password').value='%s';", pin) +
                            "document.forms[1].submit();" +
                            "})()");
                    progress.setProgress((int)(1/totalSteps*100), true);
                } else if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Account/Personal")) {
                    webview.evaluateJavascript("document.getElementsByClassName('ow-value')[0].innerHTML",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    TextView textView = findViewById(R.id.name);
                                    textView.setText(String.format("Hello %s", value.replace("\"", "")));
                                }
                            });
                    webview.evaluateJavascript("document.getElementsByClassName('ow-value')[1].innerHTML",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    TextView textView = findViewById(R.id.cardNum);
                                    textView.setText(String.format("Card Number: %s",
                                            value.replace("\"", "")
                                                    .replace(" ", "")));
                                }
                            });
                    webview.loadUrl("https://watcard.uwaterloo.ca/OneWeb/Financial/Balances");
                    progress.setProgress((int)(2/totalSteps*100), true);
                } else if (url.equals("https://watcard.uwaterloo.ca/OneWeb/Financial/Balances")) {
                    webview.evaluateJavascript(
                            "(function() {for (let tr of document.querySelectorAll('tr')) " +
                                    "{if (tr.cells[0].innerHTML == '1') " +
                                    "{return tr.cells[3].innerHTML}}})();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    TextView textView = findViewById(R.id.meal);
                                    textView.setText(String.format("Meal Plan: $%s", value.replaceAll("[^\\d.]", "" )));
                                }
                            });
                    webview.evaluateJavascript(
                            "(function() {for (let tr of document.querySelectorAll('tr')) " +
                                    "{if (tr.cells[0].innerHTML == '6') " +
                                    "{return tr.cells[3].innerHTML}}})();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    TextView textView = findViewById(R.id.flex);
                                    textView.setText(String.format("Flex: $%s", value.replaceAll("[^\\d.]", "" )));
                                }
                            });


                    Date today = Calendar.getInstance().getTime();
                    String reportDate = df.format(today).replace("/", "%2F");


                    webview.loadUrl("https://watcard.uwaterloo.ca/OneWeb/Financial/TransactionsPass?dateFrom="
                            + reportDate + "+00%3A00%3A00&dateTo=" + reportDate + "+23%3A59%3A59&returnRows=1000&_=1545508546483");
                    progress.setProgress((int)(3/totalSteps*100), true);
                } else if (url.startsWith("https://watcard.uwaterloo.ca/OneWeb/Financial/TransactionsPass?")) {
                    webview.evaluateJavascript(
                            "(function(){var amount = 0;for (let tr of document.querySelectorAll('tr')) " +
                                    "{if (tr.cells[1].getAttribute('data-title') == 'Amount:') " +
                                    "{amount = amount + parseFloat(tr.cells[1].innerHTML.split('$')[1])}}" +
                                    "return amount.toFixed(2);})();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    Date today = Calendar.getInstance().getTime();
                                    String reportDate = df.format(today);

                                    TextView textView = findViewById(R.id.usage);
                                    textView.setText(String.format("Usage today: $%s (%s)", value.replaceAll("[^\\d.]", "" ), reportDate));
                                }
                            });
                    webview.loadUrl("https://uwaterloo.ca/registrar/important-dates/entry?id=42");
                    progress.setProgress((int)(4/totalSteps*100), true);
                } else if (url.equals("https://uwaterloo.ca/registrar/important-dates/entry?id=42")){
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


                            Date now = new java.util.Date();
                            long diff = TimeUnit.MILLISECONDS.toDays(lastDay.getTime() - now.getTime());
                            double meal = Double.parseDouble(mealText.getText().toString().replace("Meal Plan: $", ""));

                            lastDayText.setText(String.format("Last Day: %s (%d days away)", df.format(lastDay), diff));
                            targetText.setText(String.format("Target Today: $%.2f", meal/diff));

                        }
                    });
                    progress.setProgress((int)(5/totalSteps*100), true);
                    webview.loadUrl("https://uwaterloo.ca/food-services/menu");
                }else if(url.equals("https://uwaterloo.ca/food-services/menu")){
                    progress.setProgress((int)(6/totalSteps*100), true);
                    progress.setVisibility(View.INVISIBLE);
                }
            }
        });

        webview.loadUrl("https://watcard.uwaterloo.ca/OneWeb/Account/LogOn");
    }


}
