package com.example.practiceservice;

import android.content.Context;
import android.os.Build;
import android.os.Message;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileCopyThread extends Thread{
    public boolean isRunning;
    private String target_path;
    private String copy_path;
    private Context mContext;


    public FileCopyThread(Context context, String target_path, String copy_path){
        isRunning = true;
        this.target_path = target_path;
        this.copy_path = copy_path;
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        super.run();
        File target_file = new File(target_path);
        File copy_file = new File(copy_path);

        if ((target_file.exists() && target_file.isDirectory()) && (copy_file.exists() && copy_file.isDirectory()) || copy_file.mkdirs()) {
            try {
                long size = Arrays.stream(target_file.listFiles()).mapToLong(File::length).sum();
                long progress = 0;
                int before = 0;

                for (File subFile : target_file.listFiles()) {
                    if (subFile.exists() && subFile.isFile() && subFile.canRead()) {
                        FileInputStream fis = new FileInputStream(subFile);
                        FileOutputStream fos = new FileOutputStream(copy_path + File.separator + subFile.getName());
                        byte[] buffer = new byte[1024];
                        while (true) {
                            if(!isRunning){
                                ((SampleService) mContext).fileCopyHandler.sendEmptyMessage(Const.FILE_COPY_FAIL);
                                break;
                            }

                            int byteRead = fis.read(buffer);
                            if (byteRead == -1) {
                                break;
                            }
                            progress += byteRead;
                            int percent = (int)(progress*100.0/size);
                            if(before < percent){
                                ((SampleService) mContext).fileCopyHandler.sendMessage(Message.obtain(((SampleService) mContext).fileCopyHandler, Const.FILE_COPY_PROGRESS, percent, 0));
                                before = percent;
                            }
                            fos.write(buffer, 0, byteRead);
                        }
                        fis.close();
                        fos.flush();
                        fos.close();
                    }
                }
                isRunning = false;
                ((SampleService) mContext).fileCopyHandler.sendEmptyMessage(Const.FILE_COPY_SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ((SampleService) mContext).fileCopyHandler.sendEmptyMessage(Const.FILE_COPY_FAIL);
        }
    }
}
