package com.android.fandm.microphone;

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

    public void removeItem(int position) {
        audio_files.get(position).delete();
        audio_files.remove(position);
        notifyItemRemoved(position);
    }

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
            removeItem(position);
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



    //new class
    //container to provide easy access to view that represent each row of the list
    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView fileName;
        Button playButton;
        Button stopButton;
        Button deleteButton;
        private MediaPlayer mediaPlayer = null;
        private boolean playing = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            playButton = itemView.findViewById(R.id.playButton);
            stopButton = itemView.findViewById(R.id.stopButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        //update the view inside of the view holder with the data
        public void bind(File file) throws FileNotFoundException {
            String name = file.getName();
            fileName.setText(name);

            File tempFile = new File(file.getPath());
            FileInputStream fis = new FileInputStream(tempFile);

            playButton.setOnClickListener(v -> {
               initializeMediaPlayer(fis);
            });

            stopButton.setOnClickListener(v -> {
                if(playing){
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });

            deleteButton.setOnClickListener(v -> {
                file.delete();

            });
        }

        private void initializeMediaPlayer(FileInputStream fileInputStream) {
            if(mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(fileInputStream.getFD());
                mediaPlayer.prepare();
            } catch (Exception e) {
                Log.d("TAG",e.toString());
            }
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                playing = true;
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
                mediaPlayer = null;
                playing = false;
            });
        }
    }
}
