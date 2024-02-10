package com.example.project;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.Vector;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText usernameEditText = findViewById(R.id.username);
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        EditText fullNameEditText = findViewById(R.id.fullName); // New EditText for full name

        MaterialButton regbtn = findViewById(R.id.signupbtn);

        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String fullName = fullNameEditText.getText().toString(); // Get full name

                // Perform HTTP POST request to the server
                new RegisterTask().execute(username, email, password, fullName); // Pass full name
            }
        });
    }


    // AsyncTask to handle the registration request
    private class RegisterTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String email = params[1];
            String password = params[2];
            String fullName = params[3]; // Retrieve full name

            try {
                // Construct the URL for your PHP script
                URL url = new URL("https://exposable-jewels.000webhostapp.com/register.php");

                // Open connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    // Set connection properties
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);

                    // Create the POST data, including the full name
                    String postData = "username=" + URLEncoder.encode(username, "UTF-8") +
                            "&email=" + URLEncoder.encode(email, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8") +
                            "&fullName=" + URLEncoder.encode(fullName, "UTF-8"); // Add full name to POST data

                    // Write the data to the connection
                    try (OutputStream outputStream = urlConnection.getOutputStream()) {
                        outputStream.write(postData.getBytes("UTF-8"));
                    }

                    // Read the response
                    return readResponse(urlConnection);
                } finally {
                    // Disconnect the connection
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }

        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the result, e.g., show a Toast or update UI
            if (result.contains("SUCCESS")) {
                Toast.makeText(Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                // Redirect to MainActivity.class (Login page)
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);
                finish();  // Finish the Register activity to prevent going back to it on back press
            } else {
                Toast.makeText(Register.this, result, Toast.LENGTH_SHORT).show();
            }
        }


        private String readResponse(HttpURLConnection connection) throws IOException {
            StringBuilder response = new StringBuilder();

            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine()).append("\n");
                }
            }

            return response.toString();
        }
    }
}
