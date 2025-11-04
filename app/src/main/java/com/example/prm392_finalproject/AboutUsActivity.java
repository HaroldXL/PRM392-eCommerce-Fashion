package com.example.prm392_finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class AboutUsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private WebView mapWebView;

    // FPT University coordinates
    private static final double LATITUDE = 10.84138048567384;
    private static final double LONGITUDE = 106.80985080938517;
    private static final String MAPTILER_API_KEY = "NUPHVx5wd6v7cni2Gdov";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        initViews();
        setupToolbar();
        setupMap();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        mapWebView = findViewById(R.id.mapWebView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupMap() {
        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // Create HTML content with MapTiler map
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8' />\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no' />\n"
                +
                "    <title>FPT University Location</title>\n" +
                "    <script src='https://cdn.maptiler.com/maptiler-sdk-js/latest/maptiler-sdk.umd.min.js'></script>\n"
                +
                "    <link rel='stylesheet' href='https://cdn.maptiler.com/maptiler-sdk-js/latest/maptiler-sdk.css' />\n"
                +
                "    <style>\n" +
                "        body { margin: 0; padding: 0; }\n" +
                "        #map { position: absolute; top: 0; bottom: 0; width: 100%; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='map'></div>\n" +
                "    <script>\n" +
                "        maptilersdk.config.apiKey = '" + MAPTILER_API_KEY + "';\n" +
                "        \n" +
                "        const map = new maptilersdk.Map({\n" +
                "            container: 'map',\n" +
                "            style: maptilersdk.MapStyle.STREETS,\n" +
                "            center: [" + LONGITUDE + ", " + LATITUDE + "],\n" +
                "            zoom: 15\n" +
                "        });\n" +
                "        \n" +
                "        // Add marker for FPT University\n" +
                "        new maptilersdk.Marker({color: '#3B82F6'})\n" +
                "            .setLngLat([" + LONGITUDE + ", " + LATITUDE + "])\n" +
                "            .setPopup(\n" +
                "                new maptilersdk.Popup().setHTML(\n" +
                "                    '<div style=\"padding: 10px;\">' +\n" +
                "                    '<h3 style=\"margin: 0 0 8px 0; color: #3B82F6;\">FPT University</h3>' +\n" +
                "                    '<p style=\"margin: 0; font-size: 12px; color: #666;\">BigSize Fashion HQ</p>' +\n"
                +
                "                    '</div>'\n" +
                "                )\n" +
                "            )\n" +
                "            .addTo(map);\n" +
                "        \n" +
                "        // Add navigation control\n" +
                "        map.addControl(new maptilersdk.NavigationControl(), 'top-right');\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        mapWebView.loadDataWithBaseURL("https://bigsizefashion.com", htmlContent, "text/html", "UTF-8", null);
    }
}
