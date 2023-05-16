package uk.woolhouse.pinreal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    public static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    @Nullable
    public static String current_user(Context context) {
        return preferences(context).getString(context.getString(R.string.key_user_uid), null);
    }

    @NonNull
    public static Intent From(android.content.Context packageContext) {
        return new Intent(packageContext, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    private void login(String uid) {
        Log.i(TAG, String.format("Login: %s", uid));
        var editor = preferences(this).edit();
        editor.putString(getString(R.string.key_user_uid), uid);
        editor.apply();
    }

    private void logout() {
        login(null);
    }

    private void sync_token(DocumentReference doc) {
        var token = preferences(this).getString(getString(R.string.key_token), null);
        doc.update("tk", token);
    }

    public void signIn(View view) {
        var view_username = (EditText) findViewById(R.id.login_username);
        var view_password = (EditText) findViewById(R.id.login_password);

        Log.d(TAG, String.format("%s @ %s", view_username.getText().toString(), view_password.getText().toString()));

        var fire = FirebaseFirestore.getInstance();
        var document = fire.collection("user").document(view_username.getText().toString());
        document.get().addOnSuccessListener(doc -> {
            if (Objects.equals(doc.getString("pw"), view_password.getText().toString())) {
                login(doc.getId());
                sync_token(document);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.login_incorrect_password), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(task -> {
            Toast.makeText(this, getString(R.string.login_incorrect_username), Toast.LENGTH_SHORT).show();
        });
    }
}