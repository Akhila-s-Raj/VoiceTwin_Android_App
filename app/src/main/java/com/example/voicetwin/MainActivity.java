package com.example.voicetwin;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        // Initialize UI elements
        btnRecord = findViewById(R.id.btnRecord);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnUpload = findViewById(R.id.btnUpload);
        musicIcon = findViewById(R.id.musicIcon);

        btnPlayPause.setVisibility(Button.GONE); // Hide initially
        musicIcon.setVisibility(ImageView.GONE);

        // Start/Stop Recording
        btnRecord.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });

        // Play/Pause Recorded Audio
        btnPlayPause.setOnClickListener(v -> {
            if (!isPlaying) {
                startPlayback();
            } else {
                stopPlayback();
            }
        });

        // Upload Audio File to Firebase
        btnUpload.setOnClickListener(v -> {
            if (audioFilePath != null) {
                uploadToFirebase(audioFilePath);
            } else {
                Toast.makeText(this, "No recording found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Start Recording
    private void startRecording() {
        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/recorded_audio.mp3";

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
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            btnRecord.setText("Start Recording");
            btnPlayPause.setVisibility(Button.VISIBLE);
            musicIcon.setVisibility(ImageView.VISIBLE);
            Toast.makeText(this, "Recording Stopped & Saved", Toast.LENGTH_SHORT).show();
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
        } catch (IOException e) {
            Toast.makeText(this, "Playback Error", Toast.LENGTH_SHORT).show();
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
        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".mp3");

        fileReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
