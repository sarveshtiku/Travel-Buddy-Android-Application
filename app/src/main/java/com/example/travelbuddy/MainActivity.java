package com.example.travelbuddy;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.appwrite.Client;
import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import io.appwrite.services.Account;

import io.appwrite.coroutines.CoroutineCallback;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import io.appwrite.Client;
import io.appwrite.coroutines.CoroutineCallback;
import io.appwrite.services.Account;






public class MainActivity extends AppCompatActivity{
    public static MainActivity instance;

    ImageView imageView;
    TextView textView;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignUp;
    private Button btnSignIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnSignUp = findViewById(R.id.signUpButton);
        btnSignIn = findViewById(R.id.signInButton);


        // Initialize the Appwrite client manager
        AppwriteClientManager.initialize(this);

        btnSignUp.setOnClickListener(v -> signUp());
        btnSignIn.setOnClickListener(v -> signIn());



    }

    private void signUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] emailParts = email.split("@");
        String userId = emailParts[0];

        // Access the Appwrite client and account instances
        Client client = AppwriteClientManager.getClient();
        Account account = AppwriteClientManager.getAccount();

        // Use the list() method to check if the account already exists
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Create a new account
                account.create(
                        userId,
                        email,
                        password,
                        new CoroutineCallback<>((result, error) -> {
                            if (error != null) {
                                error.printStackTrace();
                                return;
                            }

                            Log.d("Appwrite", result.toString());
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Successfully signed up!", Toast.LENGTH_SHORT).show();
                            });
                        })

                );
            } catch (AppwriteException e) {
                Log.e("Appwrite", "AppwriteException: " + e.getMessage());
            }
        });
    }


    private void signIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Access the Appwrite client and account instances
        Client client = AppwriteClientManager.getClient();
        Account account = AppwriteClientManager.getAccount();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            account.createEmailSession(
                    email,
                    password,
                    new CoroutineCallback<>((result, error) -> {
                        if (error != null) {
                            error.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to sign in.", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        Log.d("Appwrite", result.toString());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Successfully signed in!", Toast.LENGTH_SHORT).show());
                        // Start ExploreActivity
                        Intent intent = new Intent(MainActivity.this, ThingstodoActivity.class);
                        startActivity(intent);
                    })
            );
        });
    }

}
