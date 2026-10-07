package com.example.ocio_eventdriven_activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private LinearLayout loginLayout;
    private LinearLayout signUpLayout;
    private LinearLayout otpLayout;
    private LinearLayout successLayout;

    private EditText usernameEditText;
    private EditText loginPasswordEditText;

    private EditText firstNameEditText;
    private EditText lastNameEditText;

    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private EditText otpEditText;
    private TextView otpDisplayTextView;
    private TextView otpTimerTextView;
    private TextView usersTextView;

    private String generatedOtp;
    private CountDownTimer otpTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginLayout = findViewById(R.id.loginLayout);
        signUpLayout = findViewById(R.id.signUpLayout);
        otpLayout = findViewById(R.id.otpLayout);
        successLayout = findViewById(R.id.successLayout);

        usernameEditText = findViewById(R.id.usernameEditText);
        loginPasswordEditText = findViewById(R.id.loginPasswordEditText);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);

        emailEditText = findViewById(R.id.emailEditText);
        confirmEmailEditText = findViewById(R.id.confirmEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        otpEditText = findViewById(R.id.otpEditText);
        otpDisplayTextView = findViewById(R.id.otpDisplayTextView);
        otpTimerTextView = findViewById(R.id.otpTimerTextView);
        usersTextView = findViewById(R.id.usersTextView);

        cleanOldSharedPreferences();

        Button loginButton = findViewById(R.id.loginButton);
        Button openSignUpButton = findViewById(R.id.openSignUpButton);
        Button signUpButton = findViewById(R.id.signUpButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button verifyOtpButton = findViewById(R.id.verifyOtpButton);

        openSignUpButton.setOnClickListener(v -> {
            loginLayout.setVisibility(View.GONE);
            signUpLayout.setVisibility(View.VISIBLE);
            otpLayout.setVisibility(View.GONE);
            successLayout.setVisibility(View.GONE);
        });

        cancelButton.setOnClickListener(v -> {
            signUpLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        });

        signUpButton.setOnClickListener(v -> signUp());

        loginButton.setOnClickListener(v -> login());

        verifyOtpButton.setOnClickListener(v -> verifyOtp());
    }

    private void cleanOldSharedPreferences() {

        SharedPreferences sharedPreferences =
                getSharedPreferences("UserData", MODE_PRIVATE);

        sharedPreferences.edit()
                .remove("Email")
                .remove("FirstName")
                .remove("LastName")
                .remove("Password")
                .apply();
    }

    private void signUp() {

        String firstName =
                firstNameEditText.getText().toString().trim();

        String lastName =
                lastNameEditText.getText().toString().trim();

        String email =
                emailEditText.getText().toString().trim();

        String confirmEmail =
                confirmEmailEditText.getText().toString().trim();

        String password =
                passwordEditText.getText().toString();

        String confirmPassword =
                confirmPasswordEditText.getText().toString();

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || email.isEmpty()
                || confirmEmail.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            Toast.makeText(
                    this,
                    getString(R.string.please_fill_all_fields),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!email.equals(confirmEmail)) {

            Toast.makeText(
                    this,
                    getString(R.string.emails_do_not_match),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!password.equals(confirmPassword)) {

            Toast.makeText(
                    this,
                    getString(R.string.passwords_do_not_match),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        SharedPreferences sharedPreferences =
                getSharedPreferences("UserData", MODE_PRIVATE);

        String savedUsers =
                sharedPreferences.getString("Users", "[]");

        try {

            JSONArray usersArray =
                    new JSONArray(savedUsers);

            for (int i = 0; i < usersArray.length(); i++) {

                JSONObject user =
                        usersArray.getJSONObject(i);

                String savedEmail =
                        user.optString("Email", "");

                if (email.equalsIgnoreCase(savedEmail)) {

                    Toast.makeText(
                            this,
                            "Email is already registered.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Error reading saved accounts.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // NEW ACCOUNT IS SAVED LOCALLY ONLY.
        // It is NOT sent to Render.
        saveLocalAccount(
                firstName,
                lastName,
                email,
                password
        );

        Toast.makeText(
                this,
                getString(R.string.signup_successful),
                Toast.LENGTH_SHORT
        ).show();

        firstNameEditText.setText("");
        lastNameEditText.setText("");
        emailEditText.setText("");
        confirmEmailEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");

        signUpLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
    }

    private void saveLocalAccount(
            String firstName,
            String lastName,
            String email,
            String password) {

        SharedPreferences sharedPreferences =
                getSharedPreferences("UserData", MODE_PRIVATE);

        String savedUsers =
                sharedPreferences.getString("Users", "[]");

        try {

            JSONArray usersArray =
                    new JSONArray(savedUsers);

            JSONObject newUser =
                    new JSONObject();

            newUser.put("FirstName", firstName);
            newUser.put("LastName", lastName);
            newUser.put("Email", email);
            newUser.put("Password", password);

            usersArray.put(newUser);

            sharedPreferences.edit()
                    .putString(
                            "Users",
                            usersArray.toString()
                    )
                    .apply();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Could not save account locally.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void login() {

        String email =
                usernameEditText.getText().toString().trim();

        String password =
                loginPasswordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {

            Toast.makeText(
                    this,
                    getString(R.string.please_fill_all_fields),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        SharedPreferences sharedPreferences =
                getSharedPreferences("UserData", MODE_PRIVATE);

        String savedUsers =
                sharedPreferences.getString("Users", "[]");

        try {

            JSONArray usersArray =
                    new JSONArray(savedUsers);

            for (int i = 0; i < usersArray.length(); i++) {

                JSONObject user =
                        usersArray.getJSONObject(i);

                String savedEmail =
                        user.optString("Email", "");

                String savedPassword =
                        user.optString("Password", "");

                if (email.equals(savedEmail)
                        && password.equals(savedPassword)) {

                    Toast.makeText(
                            this,
                            "Login successful!",
                            Toast.LENGTH_SHORT
                    ).show();

                    showOtpPage();
                    return;
                }
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Error reading saved accounts.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        executorService.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(
                        "https://user-api-qpau.onrender.com/login"
                );

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setDoOutput(true);

                JSONObject request =
                        new JSONObject();

                request.put("Email", email);
                request.put("Password", password);

                try (OutputStream outputStream =
                             connection.getOutputStream()) {

                    byte[] input =
                            request.toString()
                                    .getBytes(StandardCharsets.UTF_8);

                    outputStream.write(input);
                }

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 400) {
                    inputStream =
                            connection.getErrorStream();
                } else {
                    inputStream =
                            connection.getInputStream();
                }

                StringBuilder response =
                        new StringBuilder();

                if (inputStream != null) {

                    try (BufferedReader reader =
                                 new BufferedReader(
                                         new InputStreamReader(inputStream))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                JSONObject jsonResponse =
                        new JSONObject(response.toString());

                String message =
                        jsonResponse.optString(
                                "message",
                                "Login failed."
                        );

                boolean success =
                        jsonResponse.optBoolean(
                                "success",
                                false
                        );

                runOnUiThread(() -> {

                    if (success) {

                        Toast.makeText(
                                MainActivity.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                        ).show();

                        showOtpPage();

                    } else {

                        Toast.makeText(
                                MainActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "Connection error: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void showOtpPage() {

        loginLayout.setVisibility(View.GONE);
        signUpLayout.setVisibility(View.GONE);
        successLayout.setVisibility(View.GONE);
        otpLayout.setVisibility(View.VISIBLE);

        otpEditText.setText("");

        generateNewOtp();

        startOtpTimer();
    }

    private void generateNewOtp() {

        Random random = new Random();

        int otpNumber =
                100000 + random.nextInt(900000);

        generatedOtp =
                String.valueOf(otpNumber);

        otpDisplayTextView.setText(
                "Your OTP: " + generatedOtp
        );

        otpEditText.setText("");
    }

    private void startOtpTimer() {

        if (otpTimer != null) {
            otpTimer.cancel();
        }

        otpTimer =
                new CountDownTimer(15000, 1000) {

                    @Override
                    public void onTick(
                            long millisUntilFinished) {

                        long seconds =
                                millisUntilFinished / 1000;

                        otpTimerTextView.setText(
                                "Expires in: "
                                        + seconds
                                        + " seconds"
                        );
                    }

                    @Override
                    public void onFinish() {

                        generateNewOtp();

                        Toast.makeText(
                                MainActivity.this,
                                "OTP expired. A new OTP was generated.",
                                Toast.LENGTH_SHORT
                        ).show();

                        startOtpTimer();
                    }

                }.start();
    }

    private void verifyOtp() {

        String enteredOtp =
                otpEditText.getText().toString().trim();

        if (enteredOtp.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter the OTP.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (generatedOtp == null) {

            Toast.makeText(
                    this,
                    "OTP expired. Please wait for a new OTP.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (enteredOtp.equals(generatedOtp)) {

            if (otpTimer != null) {
                otpTimer.cancel();
            }

            showUsersPage();

            Toast.makeText(
                    this,
                    "OTP verified successfully!",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Invalid OTP.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showUsersPage() {

        loginLayout.setVisibility(View.GONE);
        signUpLayout.setVisibility(View.GONE);
        otpLayout.setVisibility(View.GONE);
        successLayout.setVisibility(View.VISIBLE);

        usersTextView.setText("Loading users...");

        executorService.execute(() -> {

            JSONArray localUsers =
                    getLocalUsers();

            JSONArray renderUsers =
                    getRenderUsers();

            JSONArray combinedUsers =
                    combineUsers(
                            localUsers,
                            renderUsers
                    );

            String display =
                    formatUsers(combinedUsers);

            runOnUiThread(() ->
                    usersTextView.setText(display)
            );
        });
    }

    private JSONArray getLocalUsers() {

        SharedPreferences sharedPreferences =
                getSharedPreferences("UserData", MODE_PRIVATE);

        String savedUsers =
                sharedPreferences.getString("Users", "[]");

        try {

            return new JSONArray(savedUsers);

        } catch (Exception e) {

            return new JSONArray();
        }
    }

    private JSONArray getRenderUsers() {

        HttpURLConnection connection = null;

        try {

            URL url = new URL(
                    "https://user-api-qpau.onrender.com/"
            );

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            int responseCode =
                    connection.getResponseCode();

            if (responseCode < 200
                    || responseCode >= 300) {

                return new JSONArray();
            }

            InputStream inputStream =
                    connection.getInputStream();

            StringBuilder response =
                    new StringBuilder();

            if (inputStream != null) {

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(inputStream))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }

            String responseText =
                    response.toString().trim();

            if (responseText.isEmpty()) {
                return new JSONArray();
            }

            return new JSONArray(responseText);

        } catch (Exception e) {

            return new JSONArray();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONArray combineUsers(
            JSONArray localUsers,
            JSONArray renderUsers) {

        JSONArray combinedUsers =
                new JSONArray();

        Set<String> emails =
                new HashSet<>();

        try {

            for (int i = 0;
                 i < localUsers.length();
                 i++) {

                JSONObject user =
                        localUsers.getJSONObject(i);

                String email =
                        user.optString(
                                "Email",
                                ""
                        ).trim().toLowerCase();

                if (!email.isEmpty()
                        && !emails.contains(email)) {

                    combinedUsers.put(user);
                    emails.add(email);
                }
            }

            for (int i = 0;
                 i < renderUsers.length();
                 i++) {

                JSONObject user =
                        renderUsers.getJSONObject(i);

                String email =
                        user.optString(
                                "Email",
                                ""
                        ).trim().toLowerCase();

                if (!email.isEmpty()
                        && !emails.contains(email)) {

                    combinedUsers.put(user);
                    emails.add(email);
                }
            }

        } catch (Exception ignored) {
        }

        return combinedUsers;
    }

    private String formatUsers(
            JSONArray usersArray) {

        StringBuilder usersDisplay =
                new StringBuilder();

        if (usersArray.length() == 0) {

            return "No registered users found.";
        }

        try {

            usersDisplay.append(
                            "Total Users: "
                    ).append(usersArray.length())
                    .append("\n\n");

            usersDisplay.append(
                    "================================\n\n"
            );

            for (int i = 0;
                 i < usersArray.length();
                 i++) {

                JSONObject user =
                        usersArray.getJSONObject(i);

                String firstName =
                        user.optString(
                                "FirstName",
                                ""
                        );

                String lastName =
                        user.optString(
                                "LastName",
                                ""
                        );

                String email =
                        user.optString(
                                "Email",
                                ""
                        );

                usersDisplay.append(
                                "USER #"
                        ).append(i + 1)
                        .append("\n\n");

                usersDisplay.append(
                                "First Name: "
                        ).append(firstName)
                        .append("\n");

                usersDisplay.append(
                                "Last Name: "
                        ).append(lastName)
                        .append("\n");

                usersDisplay.append(
                                "Email: "
                        ).append(email)
                        .append("\n");

                if (i < usersArray.length() - 1) {

                    usersDisplay.append(
                            "\n--------------------------------\n\n"
                    );
                }
            }

        } catch (Exception e) {

            return "Unable to load users.";
        }

        return usersDisplay.toString();
    }

    @Override
    protected void onDestroy() {

        if (otpTimer != null) {
            otpTimer.cancel();
        }

        executorService.shutdown();

        super.onDestroy();
    }
}