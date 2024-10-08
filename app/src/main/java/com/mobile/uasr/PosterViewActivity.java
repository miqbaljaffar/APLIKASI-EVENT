package com.mobile.uasr;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class PosterViewActivity extends AppCompatActivity {

    private static final AtomicLong COUNTER = new AtomicLong();

    private Bitmap generateQRCode(String text) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            Log.e("PosterViewActivity", "Error generating QR code: ", e);
            return null;
        }
    }

    private long generateUniqueNumericId() {
        return COUNTER.incrementAndGet() + System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster_view);

        PhotoView fullImageView = findViewById(R.id.fullImageView);
        TextView fullImageDescription = findViewById(R.id.fullImageDescription);
        Button daftarButton = findViewById(R.id.daftarButton);
        Button backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        if (intent != null) {
            String imageUrl = intent.getStringExtra("imageUrl");
            String imageDescription = intent.getStringExtra("imageDescription");
            String title = intent.getStringExtra("title");

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(fullImageView);
            }

            if (imageDescription != null) {
                fullImageDescription.setText(imageDescription);
            }

            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String username = sharedPreferences.getString("username", "Pengguna Tidak Dikenal");

            daftarButton.setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(PosterViewActivity.this);
                builder.setTitle("Konfirmasi Pendaftaran");
                builder.setMessage("Apakah Anda yakin ingin mendaftar?");

                builder.setPositiveButton("Ya", (dialogInterface, i) -> {
                    try {
                        long registrationId = generateUniqueNumericId();
                        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                        String qrCodeData = "ID Pendaftaran: " + registrationId + "\n" +
                                "Nama Acara: " + title + "\n" +
                                "Atas Nama: " + username + "\n" +
                                "Tanggal dan Waktu Pendaftaran: " + currentDateTime;

                        Bitmap qrCodeBitmap = generateQRCode(qrCodeData);
                        if (qrCodeBitmap != null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            byte[] qrCodeByteArray = baos.toByteArray();
                            String qrCodeBase64 = Base64.encodeToString(qrCodeByteArray, Base64.DEFAULT);

                            // Save QR code data to Firebase Realtime Database
                            EventEntry eventRegistration = new EventEntry();
                            eventRegistration.setRegistrationId(String.valueOf(registrationId));
                            eventRegistration.setTitle(title);
                            eventRegistration.setUsername(username);
                            eventRegistration.setCurrentDateTime(currentDateTime);
                            eventRegistration.setQrCodeImage(qrCodeBase64); // Set QR code image as Base64 string

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("qrcodes").child(String.valueOf(registrationId));
                            databaseReference.setValue(eventRegistration).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PosterViewActivity.this, "Data pendaftaran berhasil disimpan", Toast.LENGTH_SHORT).show();
                                    Intent qrIntent = new Intent(PosterViewActivity.this, QRCodeDisplayActivity.class);
                                    qrIntent.putExtra("registrationId", String.valueOf(registrationId));
                                    startActivity(qrIntent);
                                } else {
                                    Log.e("Firebase", "Gagal menyimpan data pendaftaran: " + task.getException().getMessage());
                                    Toast.makeText(PosterViewActivity.this, "Gagal menyimpan data pendaftaran", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e("PosterViewActivity", "Gagal menghasilkan QR code.");
                            Toast.makeText(PosterViewActivity.this, "Gagal menghasilkan QR code", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("PosterViewActivity", "Error generating QR code: ", e);
                        Toast.makeText(PosterViewActivity.this, "Error generating QR code", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Tidak", (dialogInterface, i) -> dialogInterface.dismiss());

                builder.show();
            });

            backButton.setOnClickListener(view -> finish());
        } else {
            Log.e("PosterViewActivity", "Intent is null");
            Toast.makeText(this, "Gagal memuat detail poster", Toast.LENGTH_SHORT).show();
        }
    }
}
