package com.guanyf.appmanager;

import android.app.ActivityManager;
import android.view.View;
import android.widget.Toast;


import com.guanyf.util.PackageInfo;

import java.util.LinkedList;
import java.util.List;


public class Tab3 extends TabBase {


    @Override
    protected List<PackageInfo> getPackageList() {
        LinkedList<PackageInfo> x = new LinkedList<PackageInfo>();

        // 获取正在运行的进程
        ActivityManager actvityManager = (ActivityManager) ctx.getSystemService(ctx.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> procInfos = actvityManager.getRunningTasks(1000);
        for(ActivityManager.RunningTaskInfo ri: procInfos){
            PackageInfo pi = new PackageInfo(ri.baseActivity.getPackageName());
            pi.desc = "process id: " + ri.id;
            x.add(pi);
        }

        return x;
    }

    @Override
    protected int getMenuId() {
        return R.menu.menu_on_running;
    }

    @Override
    protected void menuAction(int menuid, int position, View view) {
        String pkgname = ((PackageInfo)adapter.getItem(position)).pkgname;
        switch(menuid){
            case R.id.running_kill:
                LinkedList<String> out = new LinkedList<String>();
                runCmd(new String[]{"su", "-c", "pm disable "+pkgname}, out, out);
                runCmd(new String[]{"su", "-c", "pm enable "+pkgname}, out, out);
                break;
            case R.id.fav_add:
                if(!mypkg.add(pkgname)){
                    Toast.makeText(view.getContext(), "Already exists!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
