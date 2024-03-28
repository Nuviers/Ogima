package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;

public class GroupUtils {
    private Activity activity;
    private Context context;
    public static final int MAX_NUMBER_PARTICIPANTS = 200;

    public GroupUtils(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public GroupUtils(Context context) {
        this.context = context;
    }
}
