package com.guanyf.appmanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.guanyf.util.CustomAdapter;
import com.guanyf.util.ItemClick;
import com.guanyf.util.MyPackage;
import com.guanyf.util.PackageInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class TabBase extends Fragment implements ItemClick{
    protected SharedPreferences pref = null;
    protected MyPackage mypkg;

    protected View view = null;
    protected Context ctx = null;

    protected PackageManager pm = null;
    protected CustomAdapter adapter;

    protected List<PackageInfo> pkgdata;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, null);
        pref = view.getContext().getSharedPreferences("config", 1);
        mypkg = new MyPackage(pref);
        ctx = view.getContext();
        pm = ctx.getPackageManager();

        ListView lv = (ListView)view.findViewById(R.id.list11);
        adapter = new CustomAdapter(this.getActivity());
        adapter.setClickListner(this);
        lv.setAdapter(adapter);

        new AsyncTask<Void, Void, Boolean>() {
            private List<PackageInfo> data;
            protected Boolean doInBackground(Void... params) {
                data = genInfos(getPackageList());
                return data != null;
            }
            protected void onPostExecute(Boolean result) {
                if(result.booleanValue()) {
                    pkgdata = data;
                    adapter.setData(data);
                    removeNotFound();
                }
            }
        }.execute();

        return view;
    }

    @Override
    public void onItemClick(final View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(this.getActivity()
                .getApplicationContext(), view);
        popupMenu.getMenuInflater().inflate(getMenuId(),
                popupMenu.getMenu());
        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        menuAction(item.getItemId(), position, view);
                        return false;
                    }
                });
        //popupMenu.getMenu().getItem(0).setEnabled(false);
        popupMenu.show();
    }

    protected void runCmd(String[] cmd, List<String> stdout, List<String> stderr) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            // list.add("==STDOUT");
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            while (true) {
                String line = bf.readLine();
                if (line == null)
                    break;
                if (stdout != null)
                    stdout.add(line.substring(line.indexOf(':') + 1));
            }
            // list.add("==STDERR");
            bf = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while (true) {
                String line = bf.readLine();
                if (line == null)
                    break;
                if (stderr != null)
                    stderr.add(line.substring(line.indexOf(':') + 1));
            }
        } catch (Exception ex) {
            if (stderr != null)
                stderr.add("Failed:" + ex);
        } finally {
            if (p != null)
                p.destroy();
        }
    }

    protected String join(Collection<String> vals, String sep) {
        StringBuffer sb = new StringBuffer();
        for (String s : vals) {
            sb.append(s).append(sep);
        }
        return sb.toString();
    }

    protected void uninstallAppSU(final String pkgname, final View item) {
        new AlertDialog.Builder(ctx)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Uninstall " + pkgname)
                .setMessage("Are you sure you want to uninstall it?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                List<String> out = new ArrayList<String>();
                                runCmd(new String[]{"su", "-c",
                                                "pm uninstall " + pkgname}, out,
                                        out);
                                String key = "package:" + pkgname;
                                if (pref.contains(key)) {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.remove(key);
                                    editor.commit();
                                }
                                Toast.makeText(ctx, join(out, "\n"),
                                        Toast.LENGTH_SHORT).show();
                                updatePkg(pkgname, item, "remove"); // loadContent();
                            }

                        }).setNegativeButton("No", null).show();
    }

    protected void uninstallApp(final String pkgname, final View item) {
        Uri packageURI = Uri.parse("package:" + pkgname);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);
    }

    void updatePkg(String pkgname, View view, String op) {
        String status = null;
        if("remove".equals(op)){
            status = "removed";
        }else if("update".equals(op)){
            try {
                ApplicationInfo aInfo = pm.getPackageInfo(pkgname,
                        PackageManager.GET_ACTIVITIES).applicationInfo;
                status = aInfo.enabled ? "enabled" : "disabled";
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }

        TextView txt = (TextView) view.findViewById(R.id.pkg_package);
        if ("enabled".equals(status))
            txt.setTextAppearance(ctx, R.style.normalText);
        else
            txt.setTextAppearance(ctx, R.style.boldText);
        txt = (TextView)view.findViewById(R.id.pkg_status);
        txt.setText(status);
    }


    ////////////////////////////////////////////
    // generate list of package info
    ///////////////////////////////////////////
    private final int newImageHeight = 128;
    private final int newImageWidth = 128;

    private Drawable ResizeImage(Drawable image) {
        try {
            Bitmap bMap = ((BitmapDrawable) image).getBitmap();
            Drawable drawable = new BitmapDrawable(ctx.getResources(),
                    getResizedBitmap(bMap, newImageHeight, (int) newImageWidth));
            return drawable;
        }catch(Throwable ex){
            return image;
        }

    }

    private LinkedList<String> notfoundpkg = new LinkedList<String>(); //这个用于卸载应用后， fav_list 里还没删除的，需要在加载的时候发现，然后从fav中删除

    private void removeNotFound() {
        if(notfoundpkg.size() == 0)
            return;

        SharedPreferences.Editor editor = pref.edit();
        for(String pkgname: notfoundpkg) {
            String key = "package:"+pkgname;
            if (!pref.contains(key)) {
                continue;
            }
            editor.remove(key);
        }
        editor.commit();
    }


    /************************ Resize Bitmap *********************************/
    private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    private List<PackageInfo> genInfos(List<PackageInfo> pkgs) {
        ApplicationInfo aInfo = null;
        PackageManager pm = ctx.getPackageManager();

        notfoundpkg.clear();

        for(PackageInfo pi: pkgs){
            try {
                //aInfo = pm.getPackageInfo(pkgname, PackageManager.GET_ACTIVITIES).applicationInfo;
                aInfo = pm.getPackageInfo(pi.pkgname, PackageManager.GET_META_DATA).applicationInfo;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                notfoundpkg.add(pi.pkgname);
                continue;
            }catch(Throwable e){
                e.printStackTrace();
                continue;
            }

            pi.setInfo(aInfo, pm);
            pi.image = ResizeImage(aInfo.loadIcon(pm));
        }

        return pkgs;
    }


    protected abstract List<PackageInfo> getPackageList();

    protected abstract int getMenuId();

    protected abstract void menuAction(int menuid, int position, View view);

}
