package com.mobile.uasr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword;
    RadioButton radioMale, radioFemale;
    CheckBox checkboxReading, checkboxWriting, checkboxDrawing;
    Button signupButton;
    TextView loginRedirectText; // Deklarasi TextView untuk redirect

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        radioMale = findViewById(R.id.radio_male);
        radioFemale = findViewById(R.id.radio_female);
        checkboxReading = findViewById(R.id.checkbox_reading);
        checkboxWriting = findViewById(R.id.checkbox_writing);
        checkboxDrawing = findViewById(R.id.checkbox_drawing);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText); // Inisialisasi loginRedirectText

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent untuk berpindah ke LoginActivity
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateForm()) {
                    return;
                }
                registerUser();
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String name = signupName.getText().toString();
        if (name.isEmpty()) {
            signupName.setError("Nama wajib diisi.");
            valid = false;
        } else {
            signupName.setError(null);
        }

        String email = signupEmail.getText().toString();
        if (email.isEmpty()) {
            signupEmail.setError("Email wajib diisi.");
            valid = false;
        } else {
            signupEmail.setError(null);
        }

        String username = signupUsername.getText().toString();
        if (username.isEmpty()) {
            signupUsername.setError("Username wajib diisi.");
            valid = false;
        } else {
            signupUsername.setError(null);
        }

        String password = signupPassword.getText().toString();
        if (password.isEmpty()) {
            signupPassword.setError("Kata sandi wajib diisi.");
            valid = false;
        } else {
            signupPassword.setError(null);
        }

        // Validasi gender (opsional)
        if (!radioMale.isChecked() && !radioFemale.isChecked()) {
            Toast.makeText(this, "Silakan pilih jenis kelamin", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void registerUser() {
        String name = signupName.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String username = signupUsername.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();
        String gender = radioMale.isChecked() ? "Laki-laki" : "Perempuan";
        boolean reading = checkboxReading.isChecked();
        boolean writing = checkboxWriting.isChecked();
        boolean drawing = checkboxDrawing.isChecked();

        User user = new User(username, password, name, email, gender, reading, writing, drawing);

        databaseReference.child(username).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SignupActivity.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this, "Registrasi gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
