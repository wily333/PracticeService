package com.example.practiceservice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class DialogGetter {

    public static DialogInterface.OnClickListener dismissClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    public static AlertDialog create(Context context, String title, String message,
                                     String positiveBtn, DialogInterface.OnClickListener positiveListener,
                                     String negativeBtn, DialogInterface.OnClickListener negativeListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(title!=null) builder.setTitle(title);
        builder.setMessage(message);

        if(positiveBtn!=null){
            if(positiveListener == null)builder.setPositiveButton(positiveBtn, dismissClickListener);
            else builder.setPositiveButton(positiveBtn, positiveListener);
        }

        if(negativeBtn!=null){
            if(negativeListener == null) builder.setNegativeButton(negativeBtn, dismissClickListener);
            else builder.setNegativeButton(negativeBtn, negativeListener);
        }
        return builder.create();
    }
}
