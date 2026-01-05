package com.example.mama.sport;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mama.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private WebView webView;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        txtTitle = findViewById(R.id.txtVideoTitle);
        webView = findViewById(R.id.webVideoPlayer);

        String title = getIntent().getStringExtra("name");
        String urlVisible = getIntent().getStringExtra("url");

        if (title != null) txtTitle.setText(title);

        if (urlVisible != null) {
            String videoId = extractVideoId(urlVisible);
            String embedUrl = "https://www.youtube.com/embed/" + videoId;
            
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);

            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(new WebChromeClient());
            
            webView.loadUrl(embedUrl);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private String extractVideoId(String url) {
        // Simple extraction for watch?v= format
        if (url.contains("v=")) {
            return url.split("v=")[1].split("&")[0];
        }
        return "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
        }
    }
}
