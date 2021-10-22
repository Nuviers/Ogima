package com.example.ogima.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.model.StatusModel;

import java.util.ArrayList;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    ArrayList <StatusModel> statusModels;
    Context context;

    public StatusAdapter (Context context, ArrayList<StatusModel> statusModels){
        this.context = context;
        this.statusModels = statusModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Creating View

        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.status_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //Set image to ImageView

        holder.imageViewStatus.setImageResource(statusModels.get(position).getResourceStatus());

        //Set text to TextView
        holder.textViewStatus.setText(statusModels.get(position).getNameUser());

    }

    @Override
    public int getItemCount() {
        return statusModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewStatus;
        TextView textViewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);

        }
    }
}
