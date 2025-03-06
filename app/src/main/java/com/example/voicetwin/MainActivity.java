package com.example.voicetwin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button btnRecord, btnPlayPause, btnUpload;
    private ImageView musicIcon;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFilePath;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private StorageReference storageReference;

    private static final int REQUEST_PERMISSION_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions at runtime
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        // Initialize UI elements
        btnRecord = findViewById(R.id.btnRecord);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnUpload = findViewById(R.id.btnUpload);
        musicIcon = findViewById(R.id.musicIcon);

        btnPlayPause.setVisibility(Button.GONE); // Hide initially
        musicIcon.setVisibility(ImageView.GONE);

        // Set file path for recorded audio
        audioFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/recorded_audio.3gp";

        btnRecord.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });

        btnPlayPause.setOnClickListener(v -> {
            if (!isPlaying) {
                startPlayback();
            } else {
                stopPlayback();
            }
        });

        btnUpload.setOnClickListener(v -> {
            if (audioFilePath != null) {
                uploadToFirebase(audioFilePath);
            } else {
                Toast.makeText(this, "No recording found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Check Permissions
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // 🔹 Request Permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_CODE);
    }

    // 🔹 Handle Permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔹 Start Recording
    private void startRecording() {
        if (!checkPermissions()) {
            requestPermissions();
            return;
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            btnRecord.setText("Stop Recording");
            Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Recording Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 Stop Recording
    private void stopRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                btnRecord.setText("Start Recording");
                btnPlayPause.setVisibility(Button.VISIBLE);
                musicIcon.setVisibility(ImageView.VISIBLE);
                Toast.makeText(this, "Recording Saved", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error Stopping Recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 Start Playback
    private void startPlayback() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setText("Pause");
            mediaPlayer.setOnCompletionListener(mp -> stopPlayback());
        } catch (IOException e) {
            Toast.makeText(this, "Playback Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 Stop Playback
    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            btnPlayPause.setText("Play");
        }
    }

    // 🔹 Upload to Firebase
    private void uploadToFirebase(String filePath) {
        Uri fileUri = Uri.fromFile(new File(filePath));
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".3gp");

        fileReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
