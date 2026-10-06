package com.example.ocio_eventdriven_activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
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

    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private EditText otpEditText;
    private TextView otpDisplayTextView;
    private TextView otpTimerTextView;

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

        emailEditText = findViewById(R.id.emailEditText);
        confirmEmailEditText = findViewById(R.id.confirmEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        otpEditText = findViewById(R.id.otpEditText);
        otpDisplayTextView = findViewById(R.id.otpDisplayTextView);
        otpTimerTextView = findViewById(R.id.otpTimerTextView);

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

    private void signUp() {

        String email = emailEditText.getText().toString().trim();
        String confirmEmail = confirmEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (email.isEmpty()
                || confirmEmail.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            Toast.makeText(
                    this,
                    getString(R.string.please_fill_all_fields),
                    Toast.LENGTH_SHORT
            ).show();

        } else if (!email.equals(confirmEmail)) {

            Toast.makeText(
                    this,
                    getString(R.string.emails_do_not_match),
                    Toast.LENGTH_SHORT
            ).show();

        } else if (!password.equals(confirmPassword)) {

            Toast.makeText(
                    this,
                    getString(R.string.passwords_do_not_match),
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    getString(R.string.signup_successful),
                    Toast.LENGTH_SHORT
            ).show();

            signUpLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }
    }

    private void login() {

        String email = usernameEditText.getText().toString().trim();
        String password = loginPasswordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {

            Toast.makeText(
                    this,
                    getString(R.string.please_fill_all_fields),
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

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);

                JSONObject request = new JSONObject();

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
                    inputStream = connection.getErrorStream();
                } else {
                    inputStream = connection.getInputStream();
                }

                StringBuilder response =
                        new StringBuilder();

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(inputStream))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
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

        Random random = new Random();

        int otpNumber =
                100000 + random.nextInt(900000);

        generatedOtp = String.valueOf(otpNumber);

        otpDisplayTextView.setText(
                "Your OTP: " + generatedOtp
        );

        startOtpTimer();
    }

    private void startOtpTimer() {

        if (otpTimer != null) {
            otpTimer.cancel();
        }

        otpTimer = new CountDownTimer(15000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

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

                otpTimerTextView.setText(
                        "OTP expired."
                );

                generatedOtp = null;

                Toast.makeText(
                        MainActivity.this,
                        "OTP expired. Please login again.",
                        Toast.LENGTH_LONG
                ).show();
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
                    "OTP expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (enteredOtp.equals(generatedOtp)) {

            if (otpTimer != null) {
                otpTimer.cancel();
            }

            otpLayout.setVisibility(View.GONE);
            successLayout.setVisibility(View.VISIBLE);

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

    @Override
    protected void onDestroy() {

        if (otpTimer != null) {
            otpTimer.cancel();
        }

        executorService.shutdown();

        super.onDestroy();
    }
}