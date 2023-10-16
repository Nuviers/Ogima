package com.example.ogima.helper;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class DialogUtils {

    private BottomSheetDialog dialog;

    public DialogUtils(BottomSheetDialog dialog) {
        this.dialog = dialog;
    }

    public void fecharBottomDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
