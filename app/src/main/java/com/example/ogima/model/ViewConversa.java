package com.example.ogima.model;

import java.io.Serializable;

public class ViewConversa implements Serializable {
    private boolean viewConversa;

    public ViewConversa(boolean viewConversa) {
        this.viewConversa = viewConversa;
    }

    public boolean isViewConversa() {
        return viewConversa;
    }

    public void setViewConversa(boolean viewConversa) {
        this.viewConversa = viewConversa;
    }
}
