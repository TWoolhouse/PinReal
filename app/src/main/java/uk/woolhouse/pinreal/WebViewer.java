package uk.woolhouse.pinreal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

public class WebViewer extends WebViewClientCompat {
    private static final String TAG = "WebViewer";

    private final WebViewAssetLoader mAssetLoader;

    WebViewer(WebViewAssetLoader assetLoader) {
        mAssetLoader = assetLoader;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void open(Context context, WebView webview) {
        var loader = new WebViewAssetLoader.Builder().addPathHandler("/", new WebViewAssetLoader.AssetsPathHandler(context));
        var settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webview.setWebViewClient(new WebViewer(loader.build()));
        webview.loadUrl("https://appassets.androidplatform.net/index.html");
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    @RequiresApi(21)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return mAssetLoader.shouldInterceptRequest(request.getUrl());
    }

    @Override
    @SuppressWarnings("deprecation") // to support API < 21
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
    }
}
