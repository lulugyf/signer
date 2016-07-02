package com.guanyf.appmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.guanyf.util.PackageInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Tab2 extends TabBase{



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        EditText et = (EditText)v.findViewById(R.id.editText_earch);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et.setVisibility(View.VISIBLE);
        return v;
    }

    protected void menuAction(int menuid, int position, View view){
        PackageInfo pi = (PackageInfo)adapter.getItem(position);
        String key = "package:"+pi.pkgname;
        switch(menuid){
            case R.id.fav_add:
                if(!mypkg.add(pi.pkgname)){
                    Toast.makeText(view.getContext(), "Already exists!"+pi.pkgname, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(view.getContext(), "Add! "+pi.pkgname, Toast.LENGTH_LONG).show();
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
