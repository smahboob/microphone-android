package com.android.fandm.microphone;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class RecordingActivity extends AppCompatActivity {

    Chronometer chronometer;
    FloatingActionButton recording_button;
    MediaRecorder mediaRecorder;
    private boolean recording = false;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private static String fileName = null;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    @SuppressLint("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        chronometer = findViewById(R.id.timerChronometer);
        recording_button = findViewById(R.id.recordingButton);
        mediaRecorder = new MediaRecorder();
        fileName = Environment.getDataDirectory().getAbsolutePath() + "/AudioRecording.3gp";

        recording_button.setOnClickListener(v -> {
            updateRecordingStatus();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceType")
    private void updateRecordingStatus() {
        if(!recording) {
            if(permissionsGranted()) {
                chronometer.start();
                recording_button.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.on)));
                recording_button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.onBackground)));
                recording = true;
            }
            else{
                requestPermissions();
            }
        }
        else{
            chronometer.stop();
            recording_button.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.off)));
            recording_button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.offBackground)));
            recording = false;
        }
    }

    //check to see if already have permission or not
    private boolean permissionsGranted() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    //if don't have permission then request for permission
    private void requestPermissions() {
        ActivityCompat.requestPermissions(RecordingActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecord && permissionToStore) {
                    Snackbar.make(findViewById(R.id.recordingActivity), "Permission Granted!", Snackbar.LENGTH_LONG).show();
                    updateRecordingStatus();
                } else {
                    Snackbar.make(findViewById(R.id.recordingActivity), "Permission Denied!", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }
}

