package com.mobile.uasr;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.IOException;
import java.io.OutputStream;

public class QRCodeDisplayActivity extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private Bitmap decodedByte;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String CHANNEL_ID = "download_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_display);

        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        Button saveButton = findViewById(R.id.saveButton);
        Button backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        String registrationId = intent.getStringExtra("registrationId");

        if (registrationId != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("qrcodes").child(registrationId);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        EventEntry event = dataSnapshot.getValue(EventEntry.class);
                        if (event != null) {
                            String qrCodeData = "ID Pendaftaran: " + event.getRegistrationId() + "\n" +
                                    "Nama Acara: " + event.getTitle() + "\n" +
                                    "Atas Nama: " + event.getUsername() + "\n" +
                                    "Tanggal dan Waktu Pendaftaran: " + event.getCurrentDateTime();

                            Bitmap qrCodeBitmap = generateQRCode(qrCodeData);
                            if (qrCodeBitmap != null) {
                                qrCodeImageView.setImageBitmap(qrCodeBitmap);
                                decodedByte = qrCodeBitmap; // Initialize decodedByte
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("QRCodeDisplay", "Failed to load QR code data: " + databaseError.getMessage());
                }
            });
        }

        saveButton.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Download QR Code")
                    .setMessage("Apakah Anda ingin mengunduh QR Code ini?")
                    .setPositiveButton("Iya", (dialog, which) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            saveQRCodeToLocalStorage();
                        } else {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {
                                saveQRCodeToLocalStorage();
                            } else {
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                            }
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });

        backButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(QRCodeDisplayActivity.this, BerandaPosterActivity.class);
            startActivity(backIntent);
            finish();
        });

        createNotificationChannel();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void saveQRCodeToLocalStorage() {
        if (decodedByte == null) {
            Toast.makeText(this, "QR Code belum dimuat", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "QRCode_" + System.currentTimeMillis() + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            showDownloadNotification("Download dimulai", "Mengunduh QR Code...");
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                decodedByte.compress(Bitmap.CompressFormat.PNG, 100, out);
                showDownloadNotification("Download selesai", "QR Code berhasil disimpan.");
                Toast.makeText(this, "QR Code berhasil disimpan", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal menyimpan QR Code", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Download Channel";
            String description = "Channel untuk notifikasi download";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showDownloadNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveQRCodeToLocalStorage();
            } else {
                Toast.makeText(this, "Izin untuk menulis ke penyimpanan eksternal ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap generateQRCode(String text) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            Log.e("QRCodeDisplayActivity", "Error generating QR code: ", e);
            return null;
        }
    }
}
