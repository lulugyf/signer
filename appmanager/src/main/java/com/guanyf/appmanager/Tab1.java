package com.guanyf.appmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.guanyf.util.PackageInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Tab1 extends TabBase {
    final static String tag = "com.guanyf.appmanager";

    protected void menuAction(int menuid, int position, View item){
        PackageInfo pi = (PackageInfo)adapter.getItem(position);
        String key = "package:"+pi.pkgname;
        List<String> out = null;
        boolean op_flag = true;  // if needed to adjust its order
        SharedPreferences.Editor editor = null;
        switch(menuid){
            case R.id.fav_remove:
                mypkg.remove(pi.pkgname);
                updatePkg(pi.pkgname, item, "remove"); //loadContent();
                op_flag = false;
                break;
            case R.id.app_disable:
                out = new LinkedList<String>();
                if(pi.enabled)
                    runCmd(new String[]{"su", "-c", "pm disable "+pi.pkgname}, out, out);
                else
                    runCmd(new String[]{"su", "-c", "pm enable "+pi.pkgname}, out, out);
                updatePkg(pi.pkgname, item, "update"); //loadContent();
                pi.enabled = !pi.enabled;
                Toast.makeText(ctx, "App "+pi.pkgname+" "+(pi.enabled?"enabled":"disabled")+ " output["+join(out, "\n")+"]", Toast.LENGTH_SHORT).show();
                break;
            case R.id.app_uninst:
                uninstallAppSU(pi.pkgname, item);
                op_flag = false;
                break;
            case R.id.app_uninst_nm:
                uninstallApp(pi.pkgname, item);
                mypkg.remove(pi.pkgname);
                op_flag = false;
                break;
            case R.id.app_launch:
                if(pi.enabled){
                    Intent intent = pm.getLaunchIntentForPackage(pi.pkgname);
                    ctx.startActivity(intent);
                }else{
                    runCmd(new String[]{"su", "-c", "pm enable "+pi.pkgname}, out, out);
                    updatePkg(pi.pkgname, item, "update"); //loadContent();
                    pi.enabled = !pi.enabled;
                    Intent intent = pm.getLaunchIntentForPackage(pi.pkgname);
                    ctx.startActivity(intent);
                    //Toast.makeText(ctx, "App disabled!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fav_refresh:
                refresh();
                break;
        }
        if(op_flag){ // update the time of package change
            mypkg.update(pi.pkgname);
        }
    }

    protected void refresh() {
        new AsyncTask<Void, Void, Boolean>() {
            private List<PackageInfo> data;
            protected Boolean doInBackground(Void... params) {
                List<PackageInfo> pkglist = getPackageList();
                for(PackageInfo pi: pkglist){
                    Log.d(tag, "--  "+pi.pkgname);
                }
                Log.d(tag, "--size " + pkglist.size());
                data = genInfos(pkglist);
                return data != null;
            }
            protected void onPostExecute(Boolean result) {
                if(result.booleanValue()) {
                    adapter.setData(data);
                    Log.d(tag, "adapter size: "+adapter.getCount());
                }
            }
        }.execute();
    }

    /**
     * read config package list, and sort them by update time desc
     */
    protected List<PackageInfo> getPackageList() {
        //packageOrders = new Hashtable<String, Long>();
    	List<PackageInfo> list = new ArrayList<PackageInfo>();
        ArrayList<PkgTime> list1 = new ArrayList<PkgTime>();
    	for(String key: pref.getAll().keySet()){
    		if(!key.startsWith("package:"))
    			continue;
            String pkg = key.substring(key.indexOf(':')+1);
            list1.add(new PkgTime(pkg, pref.getLong(key, 0L)));
    	}
        Collections.sort(list1, new Comparator<PkgTime>() {
            @Override
            public int compare(PkgTime t1, PkgTime t2) {
                if(t2.updateTime < t1.updateTime)
                    return -1;
                else if(t2.updateTime > t1.updateTime)
                    return 1;
                return 0;
            }
        });
        for(PkgTime pt: list1)
            list.add(new PackageInfo(pt.pkgname));
    	return list;
    }

    class PkgTime {
        String pkgname;
        long   updateTime;
        PkgTime(String p, long t) { pkgname = p; updateTime = t;}
    }

    protected int getMenuId(){
    	return R.menu.menu_on_fav;
    }
}
