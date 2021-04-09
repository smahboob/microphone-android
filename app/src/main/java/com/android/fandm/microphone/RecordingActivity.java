package com.android.fandm.microphone;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Objects;

public class RecordingActivity extends AppCompatActivity {

    Chronometer chronometer;
    FloatingActionButton recording_button;
    MediaRecorder mediaRecorder;
    private boolean recording = false;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private String recordFile;
    private RecyclerView audio_list_recycler_view;
    private ArrayList<File> audio_files;
    private RecordingAdapter recordingAdapter;
    private MediaPlayer mediaPlayer = null;

    @Override
    @SuppressLint("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        chronometer = findViewById(R.id.timerChronometer);
        recording_button = findViewById(R.id.recordingButton);
        audio_list_recycler_view = findViewById(R.id.audioFilesRecycler);

        updateRecyclerView();

        recording_button.setOnClickListener(v -> {
            updateRecordingStatus();
        });
    }

    private void updateRecyclerView() {
        String path = getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        audio_files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));

        recordingAdapter = new RecordingAdapter(audio_files);
        audio_list_recycler_view.setAdapter(recordingAdapter);
        audio_list_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        audio_list_recycler_view.setHasFixedSize(true);
    }

    @SuppressLint({"ResourceType", "NewApi"})
    private void updateRecordingStatus() {
        if(!recording) {
            if(permissionsGranted()) {
                startRecording();
                chronometer.setBase(SystemClock.elapsedRealtime());
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
            if(permissionsGranted()){
                chronometer.setBase(SystemClock.elapsedRealtime());
                stopRecording();
                chronometer.stop();
                recording_button.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.off)));
                recording_button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.offBackground)));
                recording = false;
            }
        }
    }

    private void startRecording() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);

        Date now = new Date();
        String recordPath = getExternalFilesDir("/").getAbsolutePath();
        recordFile = "Audio Recording - " + dataFormat.format(now) + ".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setMaxDuration(-1); //Duration unlimited or use RECORD_MAX_DURATION

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException ise) {
            Log.d("TAG",ise.toString());
            Snackbar.make(findViewById(R.id.recordingActivity),"Failed to record!",Snackbar.LENGTH_SHORT).show();
        }
        mediaRecorder.start();
        Log.d("TAG:", "Recorded in "+ recordPath + "/" + recordFile);
        Snackbar.make(findViewById(R.id.recordingActivity),"Recording Started!",Snackbar.LENGTH_SHORT).show();

    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null; //because we initialize again when hit record
        Snackbar.make(findViewById(R.id.recordingActivity),"Recording Stopped!",Snackbar.LENGTH_SHORT).show();
        updateRecyclerView();
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
                Log.d("TAG : ", String.valueOf(grantResults[0]));
                Log.d("TAG : ", String.valueOf(grantResults[1]));

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

