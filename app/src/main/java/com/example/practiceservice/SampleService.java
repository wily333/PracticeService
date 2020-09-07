package com.example.practiceservice;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.io.File;

import static com.example.practiceservice.Const.FILE_COPY_START;

public class SampleService extends Service {
    private String TAG = "SAMPLE";

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_CALL = "ACTION_CALL";
    public static final String ACTION_CALL_OK = "ACTION_CALL_OK";
    public static final String ACTION_CALL_NO = "ACTION_CALL_NO";

    FileCopyThread fileCopyThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null)
                Toast.makeText(getApplicationContext(), action, Toast.LENGTH_LONG).show();
            switch (action) {
                case ACTION_START:
                    startService();
                    break;
                case ACTION_STOP:
                    stopService();
                    break;
                case ACTION_CALL:
                    if (fileCopyThread==null || !fileCopyThread.isRunning) showSystemDialog();
                    break;
                case ACTION_CALL_OK:
                    if (fileCopyThread==null || !fileCopyThread.isRunning) fileCopyHandler.sendEmptyMessage(FILE_COPY_START);
                    break;
                case ACTION_CALL_NO:
                    if (fileCopyThread==null || !fileCopyThread.isRunning) delayDialog();
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Broadcast Receiver
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CALL);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_CALL)) {
                Intent serviceIntent = new Intent(context, SampleService.class);
                serviceIntent.setAction(ACTION_CALL);
                context.startService(serviceIntent);
            }
        }
    };

    // ACTION_START
    private String CHANNEL_ID = "sample_channel";
    private String CHANNEL_NAME = "Sample Channel";
    public NotificationCompat.Builder builder;

    private void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        Intent appIntent = new Intent(this, MainActivity.class);
        PendingIntent appPending = PendingIntent.getActivity(this, 0, appIntent, 0);

        Intent callIntent = new Intent();
        callIntent.setAction(ACTION_CALL);
        PendingIntent callPending = PendingIntent.getBroadcast(this, 0, callIntent, 0);

        Intent exitIntent = new Intent(this, SampleService.class);
        exitIntent.setAction(ACTION_STOP);
        PendingIntent exitPending = PendingIntent.getService(this, 0, exitIntent, 0);

        builder.setSmallIcon(android.R.drawable.star_big_on)
                .setContentTitle("Title")
                .setContentText("Content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(appPending)
                .addAction(android.R.drawable.btn_default, "Call", callPending)
                .addAction(android.R.drawable.btn_default, "Exit", exitPending);
        startForeground(1, builder.build());
    }

    // ACTION_STOP
    private void stopService() {
        if (fileCopyThread != null && fileCopyThread.isRunning) {
            fileCopyThread.isRunning = false;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).deleteNotificationChannel(CHANNEL_ID);
        stopForeground(true);
        stopSelf();

    }

    // ACTION_CALL
    private void showSystemDialog() {
        AlertDialog alertDialog = DialogGetter.create(SampleService.this, "Title", "message",
                "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent serviceIntent = new Intent(getApplicationContext(), SampleService.class);
                        serviceIntent.setAction(ACTION_CALL_OK);
                        getApplicationContext().startService(serviceIntent);
                    }
                }, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent serviceIntent = new Intent(getApplicationContext(), SampleService.class);
                        serviceIntent.setAction(ACTION_CALL_NO);
                        getApplicationContext().startService(serviceIntent);
                    }
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

        } else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        alertDialog.show();
    }

    // ACTION_CALL_NO
    private void delayDialog() {
        class DelayThread extends Thread {
            public void run() {
                try {
                    Thread.sleep(3000);
                    Intent serviceIntent = new Intent(getApplicationContext(), SampleService.class);
                    serviceIntent.setAction(ACTION_CALL);
                    getApplicationContext().startService(serviceIntent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        DelayThread delayThread = new DelayThread();
        delayThread.start();
    }

    // ACTION_CALL_OK
    private final String target_path = Environment.getExternalStorageDirectory() + File.separator + "target";
    private final String copy_path = Environment.getExternalStorageDirectory() + File.separator + "copy" ;

    ProgressDialog progressDialog;

    public Handler fileCopyHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FILE_COPY_START:
                    builder.setProgress(100, 0, false);

                    progressDialog = new ProgressDialog(SampleService.this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

                    } else {
                        progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    }
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();

                    fileCopyThread = new FileCopyThread(SampleService.this, target_path, copy_path);
                    fileCopyThread.start();
                    break;
                case Const.FILE_COPY_PROGRESS:
                    builder.setProgress(100, msg.arg1, false);

                    progressDialog.setProgress(msg.arg1);
                    break;
                case Const.FILE_COPY_FAIL:
                case Const.FILE_COPY_SUCCESS:
                    fileCopyHandler.removeCallbacksAndMessages(null);

                    progressDialog.dismiss();

                    builder.setContentText("File Copy Fin " + (msg.what == Const.FILE_COPY_FAIL ? "fail" : "success"));
                    builder.setProgress(0, 0, false);
                    break;
                default:
                    break;
            }
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, builder.build());
        }
    };
}
