package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomePage extends AppCompatActivity {
    private Button button, button4;
    private TextView fullnameTxt, usernameTxt, latitudeTxt, longitudeTxt, welcomeTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        button = findViewById(R.id.button);
        button4 = findViewById(R.id.button4);
        fullnameTxt = findViewById(R.id.fullname_txt);
        usernameTxt = findViewById(R.id.username_txt);
        latitudeTxt = findViewById(R.id.latitude_txt);
        longitudeTxt = findViewById(R.id.longitude_txt);
        welcomeTxt = findViewById(R.id.welcome_txt);

        // Fetch user's details and display them
        new FetchUserDetailsTask().execute();

        // Get the username from the intent
        Intent intent = getIntent();
        if (intent.hasExtra("USERNAME")) {
            String username = intent.getStringExtra("USERNAME");
            String welcomeMessage = "Welcome, " + username + "!";
            welcomeTxt.setText(welcomeMessage);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Behold the awesomeness of my web app!!- https://exposable-jewels.000webhostapp.com/");
            startActivity(Intent.createChooser(shareIntent, null));
            return true;
        } else if (item.getItemId() == R.id.item_about) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        }else if (item.getItemId() == R.id.item_logout) {
            // Handle logout action
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to handle logout action
    private void logout() {

        // Navigate the user back to the login screen or perform any other desired action
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close the current activity
    }

    // AsyncTask to fetch the user's details
    private class FetchUserDetailsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                // URL to your backend endpoint to fetch user details
                URL url = new URL("https://exposable-jewels.000webhostapp.com/userall.php");

                // Open connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Get response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Close connection and reader
                reader.close();
                connection.disconnect();

                // Return JSON response
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    // Get the username from the intent
                    Intent intent = getIntent();
                    if (intent.hasExtra("USERNAME")) {
                        String username = intent.getStringExtra("USERNAME");

                        // Find the user with the matching username
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject user = jsonArray.getJSONObject(i);
                            if (user.getString("username").equals(username)) {
                                String fullName = user.getString("full_name");
                                String latitude = user.getString("latitude");
                                String longitude = user.getString("longitude");

                                // Display user details in TextViews
                                fullnameTxt.setText("Full Name: " + fullName);
                                usernameTxt.setText("Username: " + username);
                                latitudeTxt.setText("Latitude: " + latitude);
                                longitudeTxt.setText("Longitude: " + longitude);

                                // Break out of the loop once user details are found
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Handle JSON parsing error
                    // Display appropriate message or perform fallback action
                    fullnameTxt.setText("Error parsing user details");
                    usernameTxt.setText("");
                    latitudeTxt.setText("");
                    longitudeTxt.setText("");
                }
            } else {
                // Handle case when user details could not be fetched
                fullnameTxt.setText("User details not available");
                usernameTxt.setText("");
                latitudeTxt.setText("");
                longitudeTxt.setText("");
            }
        }
    }
}
