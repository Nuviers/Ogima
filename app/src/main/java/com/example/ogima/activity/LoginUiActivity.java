package com.example.ogima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;

public class LoginUiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_login);
    }

    public void loginEmail(View view){


        Intent intent = new Intent(LoginUiActivity.this, LoginEmailActivity.class);
        startActivity(intent);
    }
}
