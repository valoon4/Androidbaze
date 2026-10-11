package com.valoon4.timeflow;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.view.View;
import android.view.Window;
import android.graphics.Color;
import android.content.SharedPreferences;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int CSV_REQUEST = 1024;
    private WebView webView;
    private String pendingCsv = null;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(11, 17, 30));
        getWindow().setNavigationBarColor(Color.rgb(11, 17, 30));
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(11, 17, 30));
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setSupportZoom(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !url.startsWith("file:///android_asset/");
            }
        });
        webView.addJavascriptInterface(new Bridge(this), "TimeFlowNative");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class Bridge {
        private final SharedPreferences preferences;
        Bridge(Context context) {
            preferences = context.getSharedPreferences("timeflow_data_v1", MODE_PRIVATE);
        }
        @JavascriptInterface public String load() {
            return preferences.getString("state", "");
        }
        @JavascriptInterface public void save(String data) {
            if (data != null && data.length() < 10000000) {
                preferences.edit().putString("state", data).commit();
            }
        }
        @JavascriptInterface public void exportCsv(String csv) {
            runOnUiThread(() -> {
                pendingCsv = csv;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "TimeFlow-Zeiterfassung.csv");
                startActivityForResult(intent, CSV_REQUEST);
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CSV_REQUEST && resultCode == RESULT_OK && data != null && pendingCsv != null) {
            try (OutputStream stream = getContentResolver().openOutputStream(data.getData())) {
                if (stream != null) stream.write(pendingCsv.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                android.widget.Toast.makeText(this, "CSV konnte nicht gespeichert werden", android.widget.Toast.LENGTH_LONG).show();
            }
        }
        pendingCsv = null;
    }

    @Override public void onBackPressed() {
        webView.evaluateJavascript("window.appBack ? window.appBack() : false", result -> {
            if (!"true".equals(result)) MainActivity.super.onBackPressed();
        });
    }

    @Override protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
