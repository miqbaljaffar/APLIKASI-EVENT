package com.mobile.uasr;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BingungLayout extends AppCompatActivity {
    private ListView listView;
    private BingungAdapter adapter;
    private List<EventEntry> eventList;
    private List<EventEntry> filteredList;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bingung);

        listView = findViewById(R.id.listView);
        searchEditText = findViewById(R.id.searchEditText);

        eventList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new BingungAdapter(this, filteredList);
        listView.setAdapter(adapter);

        loadDataFromFirebase();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_search) {
                // Tampilkan EditText untuk pencarian
                if (searchEditText.getVisibility() == View.VISIBLE) {
                    searchEditText.setVisibility(View.GONE);
                } else {
                    searchEditText.setVisibility(View.VISIBLE);
                }
                return true;
            } else if (item.getItemId() == R.id.bottom_profile) {
                handleProfileAction();
                return true;
            } else if (item.getItemId() == R.id.bottom_exit) {
                showExitConfirmationDialog();
                return true;
            } else if (item.getItemId() == R.id.bottom_home) {
                Intent homeIntent = new Intent(BingungLayout.this, BerandaPosterActivity.class);
                startActivity(homeIntent);
                return true;
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Tidak ada tindakan yang diperlukan sebelum teks diubah
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Tidak ada tindakan yang diperlukan setelah teks diubah
            }
        });
    }

    private void loadDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("qrcodes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventEntry event = snapshot.getValue(EventEntry.class);
                    if (event != null) {
                        eventList.add(event);
                    }
                }
                filterEvents(searchEditText.getText().toString()); // Terapkan filter untuk memperbarui filteredList
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void filterEvents(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(eventList);
        } else {
            for (EventEntry event : eventList) {
                if (event.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> finishAffinity())
                .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void handleProfileAction() {
        // Implement profile functionality here
        // For example, you could show a Toast or update UI elements
        // Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
    }
}
