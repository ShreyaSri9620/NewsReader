package com.example.dell.newsreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    WebView wbView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        wbView = (WebView)findViewById(R.id.webView);

        wbView.getSettings().setJavaScriptEnabled(true);
        wbView.setWebViewClient(new WebViewClient());

        Intent intent = getIntent();
        wbView.loadData(intent.getStringExtra("content"),"text/html","UFT-8");
    }
}
