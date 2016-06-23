package com.guanyf.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Created by guanyf on 10/12/2015.
 */
public class PackageInfo {
    public String label;
    public String pkgname;
    public boolean enabled;
    public Drawable image;
    public String desc;

    public String toString() {
        return String.format("label:%s\npackage:%s\nenable:%s", label,
                pkgname, enabled ? "true" : "false");
    }

    public PackageInfo(String pkgname) {
        this.pkgname = pkgname;
    }


    public void setInfo(ApplicationInfo aInfo, PackageManager pm) {
        //pkgname = aInfo.packageName;
        if("com.android.keyguard".equals(pkgname))
            label = "[no label]";
        else
            try {
                label = aInfo.loadLabel(pm).toString();
            }catch(Throwable ex){
                label = "[no label]";
            }
        enabled = aInfo.enabled;
    }


}
