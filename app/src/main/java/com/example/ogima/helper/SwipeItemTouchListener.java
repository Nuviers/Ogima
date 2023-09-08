package com.example.ogima.helper;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.annotations.NonNull;

public class SwipeItemTouchListener implements RecyclerView.OnItemTouchListener {
    private GestureDetector gestureDetector;
    private OnSwipeListener listener;

    public SwipeItemTouchListener(Context context, final RecyclerView recyclerView, OnSwipeListener listener) {
        this.listener = listener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    View view = recyclerView.findChildViewUnder(e1.getX(), e1.getY());
                    if (view != null) {
                        int position = recyclerView.getChildAdapterPosition(view);
                        if (diffX > 0) {
                            // Deslizar para a direita (gostou do carro)
                            listener.onSwipeRight(position);
                            rotateView(view, -60); // Incline para a direita
                        } else {
                            // Deslizar para a esquerda (não gostou do carro)
                            listener.onSwipeLeft(position);
                            rotateView(view, 60); // Incline para a esquerda
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // Não é necessário implementar aqui
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // Não é necessário implementar aqui
    }

    private void rotateView(View view, float degrees) {
        view.setRotation(degrees);
    }
}


