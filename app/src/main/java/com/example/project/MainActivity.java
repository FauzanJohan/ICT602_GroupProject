package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        MaterialButton loginbtn = findViewById(R.id.loginbtn);
        MaterialButton registerBtn = findViewById(R.id.registerBtn);

        if (loginbtn != null) {
            loginbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String inputUsername = username.getText().toString();
                    String inputPassword = password.getText().toString();

                    // Perform HTTP POST request to the server for login
                    new LoginTask().execute(inputUsername, inputPassword);
                }
            });
        } else {
            // Handle error: Login button not found
            Toast.makeText(MainActivity.this, "Error: Login button not found", Toast.LENGTH_SHORT).show();
        }

        if (registerBtn != null) {
            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Redirect to RegisterActivity.class
                    Intent intent = new Intent(MainActivity.this, Register.class);
                    startActivity(intent);
                }
            });
        } else {
            // Handle error: Register button not found
            Toast.makeText(MainActivity.this, "Error: Register button not found", Toast.LENGTH_SHORT).show();
        }
    }

    // AsyncTask to handle the login request
    private class LoginTask extends AsyncTask<String, Void, String> {

        private String inputUsername;

        @Override
        protected String doInBackground(String... params) {
            inputUsername = params[0];
            String inputPassword = params[1];
            String userAgent = System.getProperty("http.agent");

            try {
                URL url = new URL("https://exposable-jewels.000webhostapp.com/login.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Send the data with user agent and session ID
                String postData = "username=" + inputUsername + "&password=" + inputPassword + "&user_agent=" + userAgent + "&session_id=" + getSessionId();
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = postData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read the response
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    return scanner.useDelimiter("\\A").next();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            handleLoginResult(result, inputUsername);
        }

        private void handleLoginResult(String result, String username) {
            if (result.equals("LOGIN_SUCCESSFUL")) {
                // Save user username in SharedPreferences
                saveUserUsername(username);

                // Update timestamp in the background
                new UpdateTimestampTask().execute();

                // Redirect to HomePage.class
                Intent intent = new Intent(MainActivity.this, HomePage.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "LOGIN FAILED !!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // AsyncTask to handle the timestamp update request
    private class UpdateTimestampTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String updateTimestampUrl = "https://exposable-jewels.000webhostapp.com/login.php";

            try {
                URL url = new URL(updateTimestampUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Add session ID to the request
                String postData = "session_id=" + getSessionId();
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = postData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read the response from the server
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    return scanner.useDelimiter("\\A").next();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the result of the timestamp update request
            if (result.equals("TIMESTAMP_UPDATED_SUCCESSFULLY")) {
                // Timestamp updated successfully
            } else {
                // Handle the case where the timestamp update failed
            }
        }
    }

    // Helper method to get the current session ID
    private String getSessionId() {
        return PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("SESSION_ID", "");
    }

    private void saveUserUsername(String username) {
        SharedPreferences sharedPref = getSharedPreferences("userSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("username", username);
        editor.apply();
    }
}
