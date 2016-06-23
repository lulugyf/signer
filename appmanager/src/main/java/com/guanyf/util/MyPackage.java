package com.guanyf.util;

import android.content.SharedPreferences;

import com.guanyf.appmanager.R;

/**
 * Created by guanyf on 2016/3/28.
 */
public class MyPackage {
    private SharedPreferences pref = null;
    private SharedPreferences.Editor editor = null;

    public MyPackage(SharedPreferences pref) {
        this.pref = pref;
    }

    public boolean remove(String pkgname){
        String key = "package:"+pkgname;

        if(!pref.contains(key)){
            return false;
        }

        editor = pref.edit();
        editor.remove(key);
        editor.commit();

        return true;
    }

    public boolean add(String pkgname) {
        String key = "package:"+pkgname;

        if(pref.contains(key)){
            return false;
        }

        editor = pref.edit();
        editor.putLong(key, System.currentTimeMillis());
        editor.commit();

        return true;
    }

    public boolean update(String pkgname) {
        String key = "package:"+pkgname;

        if(!pref.contains(key)){
            return false;
        }

        editor = pref.edit();
        editor.putLong(key, System.currentTimeMillis());
        editor.commit();

        return true;
    }
}
