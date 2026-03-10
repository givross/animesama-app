package com.animesama.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private SharedPreferences prefs;
    private static final String DEFAULT_URL = "https://anime-sama.to/";
    private static final String PREF_URL = "url";

    @Override @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("as", Context.MODE_PRIVATE);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0A0A14"));

        swipeRefresh = new SwipeRefreshLayout(this);
        swipeRefresh.setColorSchemeColors(Color.parseColor("#E63946"));
        swipeRefresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#111122"));

        webView = new WebView(this);
        swipeRefresh.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        root.addView(swipeRefresh, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        FrameLayout.LayoutParams pbParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 8);
        root.addView(progressBar, pbParams);

        TextView btnBack = new TextView(this);
        btnBack.setText("←");
        btnBack.setTextSize(24);
        btnBack.setGravity(android.view.Gravity.CENTER);
        btnBack.setBackgroundColor(Color.parseColor("#CC1A1A2F"));
        btnBack.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(130, 130);
        backParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
        backParams.setMargins(32, 0, 0, 80);
        root.addView(btnBack, backParams);

        TextView btnUrl = new TextView(this);
        btnUrl.setText("🔗");
        btnUrl.setTextSize(22);
        btnUrl.setGravity(android.view.Gravity.CENTER);
        btnUrl.setBackgroundColor(Color.parseColor("#E63946"));
        FrameLayout.LayoutParams urlParams = new FrameLayout.LayoutParams(140, 140);
        urlParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        urlParams.setMargins(0, 0, 32, 70);
        root.addView(btnUrl, urlParams);

        setContentView(root);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int p) {
                if (p < 100) { progressBar.setVisibility(View.VISIBLE); progressBar.setProgress(p); }
                else { progressBar.setVisibility(View.GONE); swipeRefresh.setRefreshing(false); }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                v.loadUrl(r.getUrl().toString()); return true;
            }
            @Override public void onPageFinished(WebView v, String url) {
                swipeRefresh.setRefreshing(false);
                v.loadUrl("javascript:(function(){var s=document.createElement('style');s.innerHTML='[class*=\"ad\"],[id*=\"ad\"]{display:none!important}';document.head.appendChild(s);})();");
            }
        });

        swipeRefresh.setOnRefreshListener(() -> webView.reload());
        btnBack.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        btnUrl.setOnClickListener(v -> showUrlDialog());
        webView.loadUrl(prefs.getString(PREF_URL, DEFAULT_URL));
    }

    private void showUrlDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 50, 60, 30);
        layout.setBackgroundColor(Color.parseColor("#111122"));
        TextView lbl = new TextView(this);
        lbl.setText("Nouvelle URL du site :");
        lbl.setTextColor(Color.parseColor("#9090BB"));
        lbl.setTextSize(14);
        lbl.setPadding(0, 0, 0, 20);
        layout.addView(lbl);
        EditText input = new EditText(this);
        input.setText(prefs.getString(PREF_URL, DEFAULT_URL));
        input.setTextColor(Color.WHITE);
        input.setBackgroundColor(Color.parseColor("#1A1A2F"));
        input.setPadding(24, 20, 24, 20);
        input.setSingleLine(true);
        layout.addView(input);
        new AlertDialog.Builder(this)
            .setTitle("🔗 Changer l'URL")
            .setView(layout)
            .setPositiveButton("✅ Sauvegarder", (d, w) -> {
                String url = input.getText().toString().trim();
                if (!url.startsWith("http")) url = "https://" + url;
                prefs.edit().putString(PREF_URL, url).apply();
                webView.loadUrl(url);
                Toast.makeText(this, "✅ URL mise à jour!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Annuler", null)
            .setNeutralButton("🔄 Reset", (d, w) -> {
                prefs.edit().putString(PREF_URL, DEFAULT_URL).apply();
                webView.loadUrl(DEFAULT_URL);
            }).show();
    }

    @Override public boolean onKeyDown(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_BACK && webView.canGoBack()) { webView.goBack(); return true; }
        return super.onKeyDown(k, e);
    }
    @Override protected void onPause() { super.onPause(); webView.onPause(); }
    @Override protected void onResume() { super.onResume(); webView.onResume(); }
    @Override protected void onDestroy() { webView.destroy(); super.onDestroy(); }
}
