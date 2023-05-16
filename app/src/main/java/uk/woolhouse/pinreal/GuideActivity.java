package uk.woolhouse.pinreal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class GuideActivity extends AppCompatActivity {

    @NonNull
    public static Intent From(android.content.Context packageContext) {
        return new Intent(packageContext, GuideActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        var webview = (WebView) findViewById(R.id.webview);
        WebViewer.open(this, webview);
    }
}