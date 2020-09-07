package com.example.practiceservice;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileCopy {
    private static String TAG = "SAMPLE";


    public static boolean copyFile(String target_path, String copy_path) {
        File target_file = new File(target_path);
        File copy_file = new File(copy_path);
        try {
            if ((copy_file.exists() && copy_file.isDirectory()) || copy_file.mkdirs()) {
                for (String subFile : target_file.list()) {
                    File temp = new File(target_path + (target_path.endsWith(File.separator) ? "" : File.separator) + subFile);

                    if (temp.isDirectory()) {
                        FileCopy.copyFile(target_path + File.separator + subFile, copy_path + File.separator + subFile);
                    } else if (!temp.exists()) {
                        return false;
                    } else if (!temp.isFile()) {
                        return false;
                    } else if (temp.canRead()) {
                        FileInputStream fileInputStream = new FileInputStream(temp);
                        FileOutputStream fileOutputStream = new FileOutputStream(copy_path + File.separator + temp.getName());
                        byte[] buffer = new byte[1024];
                        while (true) {

                            int byteRead = fileInputStream.read(buffer);
                            if (byteRead == -1) {
                                break;
                            }
                            fileOutputStream.write(buffer, 0, byteRead);
                        }
                        fileInputStream.close();
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } else {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        int count = files.length;
        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }
}
