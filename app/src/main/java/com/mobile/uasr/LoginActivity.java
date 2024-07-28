package com.mobile.uasr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button loginButton;
    RadioGroup userTypeRadioGroup;
    TextView signupRedirectText;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateUsername() || !validatePassword()) {
                    Toast.makeText(LoginActivity.this, "Harap isi kedua kolom", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser();
                }
            }
        });

        // Redirect ke SignupActivity saat signupRedirectText diklik
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Username tidak boleh kosong");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Password tidak boleh kosong");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void loginUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        int selectedId = userTypeRadioGroup.getCheckedRadioButtonId();
        RadioButton userTypeRadioButton = findViewById(selectedId);
        String userType = userTypeRadioButton.getText().toString();

        databaseReference.child(userUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getPassword().equals(userPassword)) {
                        // Simpan username ke SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", userUsername);
                        editor.apply();

                        if (userType.equals("Admin")) {
                            Intent intent = new Intent(LoginActivity.this, BerandaPosterActivity.class);
                            intent.putExtra("username", userUsername);
                            startActivity(intent);
                            finish();
                        } else if (userType.equals("Konsumen")) {
                            Intent intent = new Intent(LoginActivity.this, BerandaPosterActivity.class);
                            intent.putExtra("username", userUsername);
                            intent.putExtra("isKonsumen", userType.equals("Konsumen"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, "Tipe pengguna tidak valid", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Password tidak valid", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Pengguna tidak ada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Kesalahan database: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
