// LoginHelper.java
package com.example.memorygameteam2;

import android.os.AsyncTask;
import android.widget.EditText;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginHelper {

    public interface LoginCallback {
        void onLoginSuccess(String response);
        void onLoginFailure(String error);
    }

    public static void performLogin(EditText userEditText, EditText passwordEditText,
                                    String serverUrl, LoginCallback callback) {
        String username = userEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            callback.onLoginFailure("Username and password cannot be empty");
            return;
        }

        new LoginTask(serverUrl, callback).execute(username, password);
    }

    private static class LoginTask extends AsyncTask<String, Void, String> {
        private final String serverUrl;
        private final LoginCallback callback;
        private String error = null;

        public LoginTask(String serverUrl, LoginCallback callback) {
            this.serverUrl = serverUrl;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... credentials) {
            String username = credentials[0];
            String password = credentials[1];
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String response = null;

            try {
                URL url = new URL(serverUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                // Create JSON request body
                String jsonInputString = String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                        username, password);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    error = "Server returned HTTP " + responseCode;
                    return null;
                }

                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        responseBuilder.append(responseLine.trim());
                    }
                }
                response = responseBuilder.toString();

            } catch (Exception e) {
                error = "Error: " + e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (error != null || response == null) {
                callback.onLoginFailure(error != null ? error : "Login failed");
            } else {
                callback.onLoginSuccess(response);
            }
        }
    }
}