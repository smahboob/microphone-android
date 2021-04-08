package com.android.fandm.microphone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

//adapter takes data and puts into view holder
public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder>{


    public RecordingAdapter(List<String> items) {
        //this.items = items;
    }


    @Override
    public void onBindViewHolder(@NonNull RecordingAdapter.ViewHolder holder, int position) {

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //use layout inflater to inflate a view
        View todoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_card, parent, false);
        return new ViewHolder(todoView);
    }


    //how many items are in the list
    @Override
    public int getItemCount() {
        return 0;
    }



    //new class
    //container to provide easy access to view that represent each row of the list
    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView fileName;
        Button play;
        Button stop;
        Button delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            play = itemView.findViewById(R.id.playButton);
            stop = itemView.findViewById(R.id.stopButton);
            delete = itemView.findViewById(R.id.deleteButton);
        }

        //update the view inside of the view holder with the data
        public void bind(String item) {

        }
    }
}