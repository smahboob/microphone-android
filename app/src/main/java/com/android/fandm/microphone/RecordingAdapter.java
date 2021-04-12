package com.android.fandm.microphone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//adapter takes data and puts into view holder
public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder>{

    //private final File[] audio_files;
    private final ArrayList<File> audio_files;

    public RecordingAdapter(ArrayList<File> audio_files) {
        this.audio_files = audio_files;
    }

    public void removeItem(@NonNull RecordingAdapter.ViewHolder holder, int position) {
        if (audio_files.get(position).delete()) {
            audio_files.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, audio_files.size());
        }
        else{
            Snackbar.make(holder.itemView.getRootView().findViewById(R.id.recordingActivity),"Failed to Delete Audio File!", Snackbar.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RecordingAdapter.ViewHolder holder, int position) {
        File file = audio_files.get(position);
        try {
            holder.bind(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Button delete = holder.deleteButton;
        delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            String deleteMessage = "Are you sure you want to delete: " + audio_files.get(position).getName();
            builder.setMessage(deleteMessage).setPositiveButton("Yes", (dialog, which) -> {
                removeItem(holder, position);
                Snackbar.make(holder.itemView.getRootView().findViewById(R.id.recordingActivity),"Deleted Audio File!", Snackbar.LENGTH_SHORT).show();
            }).setNegativeButton("Cancel", null);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //use layout inflater to inflate a view
        View recordingCardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_card, parent, false);
        return new ViewHolder(recordingCardView);
    }

    @Override
    public int getItemCount() {
        return audio_files.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView fileName;
        Button playButton;
        Button stopButton;
        Button deleteButton;
        private MediaPlayer mediaPlayer = null;
        private boolean playing = false;
        private boolean paused = false;
        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            playButton = itemView.findViewById(R.id.playButton);
            stopButton = itemView.findViewById(R.id.stopButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
        private int pausedAt;

        //update the view inside of the view holder with the data
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void bind(File file) throws FileNotFoundException {
            String name = file.getName();
            fileName.setText(name);

            File tempFile = new File(file.getPath());
            FileInputStream fis = new FileInputStream(tempFile);

            playButton.setOnClickListener(v -> {
                //this is when u pause because already playing
                if(playing && !paused){
                    mediaPlayer.pause();
                    playButton.setBackground(ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.ic_media_play));
                    playButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.notInUsePlayButton)));
                    pausedAt = mediaPlayer.getCurrentPosition();
                    Snackbar.make(itemView.getRootView().findViewById(R.id.recordingActivity),"Paused Recording!", Snackbar.LENGTH_SHORT).show();
                    paused = true;
                }
                //this is case when paused in middle of playing
                else if(playing){
                    playButton.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_baseline_pause));
                    mediaPlayer.seekTo(pausedAt);
                    mediaPlayer.start();
                    Snackbar.make(itemView.getRootView().findViewById(R.id.recordingActivity),"Resumed Recording!", Snackbar.LENGTH_SHORT).show();
                    paused = false;
                }
                //this is when not playing
                else if(!paused){
                    playButton.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_baseline_pause));
                    stopButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.stopButtonWhilePlaying)));
                    initializeMediaPlayer(fis,itemView);
                }
            });

            stopButton.setOnClickListener(v -> {
                stopButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.stopButton)));
                if(playing){
                    paused = false;
                    playing = false;
                    stopButton.setEnabled(false);
                    deleteButton.setEnabled(true);
                    mediaPlayer.release();
                    mediaPlayer = null;
                    Snackbar.make(itemView.getRootView().findViewById(R.id.recordingActivity),"Stopped Playing Audio File!", Snackbar.LENGTH_SHORT).show();
                    playButton.setBackground(ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.ic_media_play));
                    playButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.notInUsePlayButton)));
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void initializeMediaPlayer(FileInputStream fileInputStream, View itemView) {
            if(mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(fileInputStream.getFD());
                mediaPlayer.prepare();
            } catch (Exception e) {
                Log.d("TAG",e.toString());
                Snackbar.make(itemView.getRootView().findViewById(R.id.recordingActivity),"Error Playing Audio File!", Snackbar.LENGTH_SHORT).show();
            }
            mediaPlayer.setOnPreparedListener(mp -> {
                stopButton.setEnabled(true);
                deleteButton.setEnabled(false);
                mediaPlayer.start();
                playing = true;
                Snackbar.make(itemView.getRootView().findViewById(R.id.recordingActivity),"Playing Audio File!", Snackbar.LENGTH_SHORT).show();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                stopButton.setEnabled(false);
                deleteButton.setEnabled(true);
                mediaPlayer.release();
                mediaPlayer = null;
                playing = false;
                paused = false;
                stopButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.stopButton)));
                playButton.setBackground(ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.ic_media_play));
                playButton.setBackgroundTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.notInUsePlayButton)));
            });
        }
    }
}
