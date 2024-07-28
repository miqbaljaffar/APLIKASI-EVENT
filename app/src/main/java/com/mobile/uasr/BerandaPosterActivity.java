package com.mobile.uasr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class BerandaPosterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private FloatingActionButton fab;
    private GridView gridView;
    private EditText searchEditText;
    private ArrayList<DataClassPoster> dataList;
    private ArrayList<DataClassPoster> filteredList;
    private MyAdapterPoster adapter;
    private ImageView selectedImageView;
    private String username;
    private boolean isKonsumen = false; // Flag untuk mengecek apakah user adalah Konsumen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beranda_poster_activity);

        fab = findViewById(R.id.fab);

        // Mendapatkan data dari intent
        username = getIntent().getStringExtra("username");
        isKonsumen = getIntent().getBooleanExtra("isKonsumen", false);

        // Sembunyikan fab jika user adalah Konsumen
        if (isKonsumen) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(v -> {
                showAddPosterDialog();
            });
        }

        gridView = findViewById(R.id.gridView);
        searchEditText = findViewById(R.id.searchEditText);
        dataList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new MyAdapterPoster(this, filteredList);
        gridView.setAdapter(adapter);

        // Memuat poster yang ada
        loadPostersFromFirebase();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_search) {
                // Mengubah visibilitas kolom pencarian
                if (searchEditText.getVisibility() == View.VISIBLE) {
                    searchEditText.setVisibility(View.GONE);
                } else {
                    searchEditText.setVisibility(View.VISIBLE);
                }
            } else if (item.getItemId() == R.id.bottom_profile) {
                // Navigasi ke DaftarPosterActivity
                Intent intent = new Intent(BerandaPosterActivity.this, BingungLayout.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.bottom_exit) {
                // Tampilkan dialog konfirmasi sebelum keluar
                showExitConfirmationDialog();
            }
            return true;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Tidak ada tindakan yang diperlukan sebelum teks diubah
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter poster berdasarkan kueri pencarian
                filterPosters(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Tidak ada tindakan yang diperlukan setelah teks diubah
            }
        });
    }

    private void loadPostersFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posters");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataClassPoster poster = snapshot.getValue(DataClassPoster.class);
                    dataList.add(poster);
                }
                filterPosters(searchEditText.getText().toString()); // Terapkan filter untuk memperbarui filteredList
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Gagal memuat poster: " + databaseError.getMessage());
            }
        });
    }

    private void filterPosters(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(dataList);
        } else {
            for (DataClassPoster poster : dataList) {
                if (poster.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(poster);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddPosterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.add_poster_dialog, null);
        builder.setView(dialogView);

        EditText inputTitle = dialogView.findViewById(R.id.inputPosterTitle);
        EditText inputDescription = dialogView.findViewById(R.id.inputPosterDescription);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        selectedImageView = dialogView.findViewById(R.id.selectedImageView);

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
        });

        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();
            if (imageUri != null && !title.isEmpty() && !description.isEmpty()) {
                savePosterToFirebase(title, description, imageUri);
            } else {
                Toast.makeText(BerandaPosterActivity.this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                if (selectedImageView != null) {
                    selectedImageView.setImageBitmap(bitmap);
                    selectedImageView.setVisibility(View.VISIBLE);
                } else {
                    Log.e("ImageViewError", "selectedImageView adalah null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ImageSelection", "Gagal memilih gambar atau tidak ada data yang dikembalikan");
        }
    }

    private void savePosterToFirebase(String title, String description, Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference posterRef = storageRef.child("posters/" + System.currentTimeMillis() + ".jpg");

        posterRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference ref = database.getReference("posters").push();
                        DataClassPoster poster = new DataClassPoster(imageUrl, title, description);
                        ref.setValue(poster).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(BerandaPosterActivity.this, "Poster berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BerandaPosterActivity.this, "Gagal menambahkan poster", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(BerandaPosterActivity.this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show());
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> finishAffinity())
                .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        // Show a dialog to confirm exit
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Call super.onBackPressed() to perform the default back action
                    super.onBackPressed();
                })
                .setNegativeButton("Tidak", (dialog, which) -> {
                    // Dismiss the dialog and do nothing
                    dialog.dismiss();
                })
                .show();
    }



}
