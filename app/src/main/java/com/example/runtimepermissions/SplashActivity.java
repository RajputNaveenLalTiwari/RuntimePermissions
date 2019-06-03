package com.example.runtimepermissions;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_RUNTIME_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(this, RuntimePermissionChecker.class);
        startActivityForResult(intent, REQUEST_CODE_RUNTIME_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RUNTIME_PERMISSION && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (requestCode == REQUEST_CODE_RUNTIME_PERMISSION && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
    }
}
