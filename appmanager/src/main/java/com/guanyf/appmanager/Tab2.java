package com.guanyf.appmanager;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import com.guanyf.util.PackageInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Tab2 extends TabBase{


    protected void menuAction(int menuid, int position, View view){
        PackageInfo pi = pkgdata.get(position);
        String key = "package:"+pi.pkgname;
        switch(menuid){
            case R.id.fav_add:
                if(!mypkg.add(pi.pkgname)){
                    Toast.makeText(view.getContext(), "Already exists!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.app_uninst:
                uninstallAppSU(pi.pkgname, view);
                break;
            case R.id.app_uninst_nm:
                uninstallApp(pi.pkgname, view);
                break;
        }
    }

    protected List<PackageInfo> getPackageList() {
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        try{
            Process p = Runtime.getRuntime().exec(new String[]{"pm", "list", "packages"});
            BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while(true){
                String line = bf.readLine();
                if(line == null)
                    break;
                list.add(new PackageInfo(line.substring(line.indexOf(':')+1)));
            }
        }catch(Exception ex){
        }
        return list;
    }

    protected int getMenuId(){
    	return R.menu.menu_on_all;
    }
}
