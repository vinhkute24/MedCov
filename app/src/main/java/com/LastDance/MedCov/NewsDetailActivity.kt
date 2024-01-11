package com.LastDance.MedCov

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView


class NewsDetailActivity : AppCompatActivity() {
    lateinit var newsWebView : WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        newsWebView = findViewById(R.id.newsWebView)

        val href = intent.getStringExtra("href")
        if (href != null) {
            newsWebView.loadUrl(href)
        }
    }
}