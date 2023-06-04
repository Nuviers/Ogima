package com.example.ogima.helper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.adapter.AdapterPostagensComunidade;

public class RecyclerViewScrollListener  extends RecyclerView.OnScrollListener {
    private LinearLayoutManager layoutManager;
    private AdapterPostagensComunidade.VideoViewHolder videoViewHolder;

    public RecyclerViewScrollListener(LinearLayoutManager layoutManager, AdapterPostagensComunidade.VideoViewHolder videoViewHolder) {
        this.layoutManager = layoutManager;
        this.videoViewHolder = videoViewHolder;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        // Verifique se o ViewHolder do ExoPlayer está totalmente visível
        boolean isViewHolderVisible = layoutManager.findFirstCompletelyVisibleItemPosition() <= videoViewHolder.getBindingAdapterPosition()
                && videoViewHolder.getBindingAdapterPosition() <= layoutManager.findLastCompletelyVisibleItemPosition();

        if (isViewHolderVisible) {
            // O ViewHolder do ExoPlayer está totalmente visível, inicie o ExoPlayer
            //videoViewHolder.iniciarExoPlayer();
        } else {
            // O ViewHolder do ExoPlayer não está totalmente visível, pare o ExoPlayer
            //videoViewHolder.pararExoPlayer();
        }
    }
}