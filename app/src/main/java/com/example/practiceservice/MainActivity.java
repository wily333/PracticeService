package com.example.practiceservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private String TAG = "SAMPLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getOverlayPermissions();
    }

    // <permission (same) >
    private void getOverlayPermissions() {
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 5469);
        } else {
            getExternalWritePermissions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5469) {
            if (Settings.canDrawOverlays(this)) {
                getExternalWritePermissions();
            } else {
                Toast.makeText(MainActivity.this, "Overlay Permission denied.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getExternalWritePermissions() {
        int WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (WRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            startSampleService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if ((grantResults.length > 0) && (grantResults[0] != 0)) {
            Toast.makeText(MainActivity.this, "External Storage Permission denied.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            startSampleService();
        }
    }
    // </permission>

    private void startSampleService() {
        if (!isServiceRunning(SampleService.class)) {
            Intent intent = new Intent(MainActivity.this, SampleService.class);
            intent.setAction(SampleService.ACTION_START);
            startService(intent);
            Toast.makeText(MainActivity.this, "Service Start", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Service isRunning", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}