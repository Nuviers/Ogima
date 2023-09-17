package com.example.ogima.helper;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.adapter.AdapterFotosPerfilParcEdicao;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final AdapterFotosPerfilParcEdicao adapter;

    public ItemTouchHelperCallback(AdapterFotosPerfilParcEdicao adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        int sourcePosition = source.getAdapterPosition();
        int targetPosition = target.getAdapterPosition();

        adapter.swapItems(sourcePosition, targetPosition); // Implemente este método no seu adapter
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // Caso queira implementar o deslizar para excluir
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true; // Habilitar arrasto longo para reordenar
    }
}