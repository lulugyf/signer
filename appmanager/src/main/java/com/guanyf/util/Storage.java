package com.guanyf.util;

import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by guanyf on 2016/6/29.
 */
public class Storage {
    private SharedPreferences pref;
    public Storage(SharedPreferences pref){
        this.pref = pref;
    }

    public File fpath() {
        return new File(Environment.getExternalStorageDirectory(), "fav.list");
        //return getInnerSDCardPath() + "/fav.list";
    }
    private String status;
    public String status() {
        return status;
    }
    private String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }


    public void exportList(String fpath) {
        //File fpath = fpath();
        FileWriter fwriter = null;
        int i=0;
        try{
            fwriter = new FileWriter(fpath);
            for(String key: pref.getAll().keySet()){
                if(!key.startsWith("package:"))
                    continue;
                String pkg = key.substring(key.indexOf(':')+1);
                long update_time = pref.getLong(key, 0L);

                fwriter.append(pkg).append(':').append(String.valueOf(update_time)).append('\n');
                i ++;
            }
            status = "export list count:"+i + " to:"+fpath;
        }catch(IOException ex){
            status = "save--- file failed:"+ex;
        }finally{
            if(fwriter != null) {
                try{ fwriter.close(); }catch(IOException ex){}
            }
        }
    }
    public void importList(String fpath) {
        BufferedReader bf = null;
        //File fpath = fpath();
        SharedPreferences.Editor editer = pref.edit();

        int i=0;
        try {
            bf = new BufferedReader(new FileReader(fpath));
            String line = null;
            while((line = bf.readLine()) != null) {
                if(line.indexOf(':') <= 0)
                    continue;
                int p = line.indexOf(':');
                String pkg = line.substring(0, p);
                long update_time = Long.parseLong(line.substring(p+1));
                String key = "package:"+pkg;
                editer.putLong(key, update_time);
                i ++;
            }
            status = "import count:"+i + " from:"+fpath;
        } catch (IOException e) {
            status = "exception of file reading:"+e;
        } finally {
            if(bf != null ) try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            editer.commit();
        }


    }
}
