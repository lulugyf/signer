package com.guanyf.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.guanyf.appmanager.R;

/**
 * Created by guanyf on 10/12/2015.
 */
public class CustomAdapter extends BaseAdapter implements View.OnClickListener, Filterable {

    /*********** Declare Used Variables *********/
    private Activity activity;
    private List<PackageInfo> data = new LinkedList<PackageInfo>();
    private List<PackageInfo> origData = null;
    private static LayoutInflater inflater=null;
    private Context context;
    private ItemClick ic;
    public Resources res;
    private boolean initialized = false;
    private ItemFilter mFilter = new ItemFilter();

    public void setData(List<PackageInfo> data) {
        this.origData = data;
        this.data = data;
        initialized = true;
        this.notifyDataSetChanged();
    }
    /*************  CustomAdapter Constructor *****************/
    public CustomAdapter(Activity a) {

        /********** Take passed values **********/
        activity = a;
        context = activity.getBaseContext();

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = (LayoutInflater)activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    public void setClickListner(ItemClick ic){
        this.ic = ic;
    }



    /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            LinkedList<PackageInfo> nlist = new LinkedList<>();
            for (PackageInfo pi: origData) {
                if(pi.pkgname.contains(filterString))
                    nlist.add(pi);
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            data = (List<PackageInfo>) results.values;
            notifyDataSetChanged();
        }

    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{
        public TextView text;
        public TextView text1;
        public TextView text2;
        public ImageView image;
    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.mylist, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.text = (TextView) vi.findViewById(R.id.pkg_name);
            holder.text1=(TextView)vi.findViewById(R.id.pkg_package);
            holder.text2=(TextView)vi.findViewById(R.id.pkg_status);
            holder.image = (ImageView)vi.findViewById(R.id.icon);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();

        if(data.size()<=0)
        {
            if(initialized)
                holder.text.setText("No Data");
            else
                holder.text.setText("Loading...");
        }
        else
        {
            /***** Get each Model object from Arraylist ********/
            PackageInfo pi = data.get( position );

            /************  Set Model values in Holder elements ***********/

            holder.text.setText( pi.label  );
            holder.text1.setText( pi.pkgname );
            holder.text2.setText(pi.desc == null ? "": pi.desc);
            if(pi.enabled){
                holder.text1.setTextAppearance(context, R.style.normalText);
            }else{
                holder.text1.setTextAppearance(context, R.style.boldText);
            }
            holder.image.setImageDrawable(pi.image);

            /******** Set Item Click Listner for LayoutInflater for each row *******/

            vi.setOnClickListener(new OnItemClickListener( position ));
        }
        return vi;
    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {
            if(ic != null )
                ic.onItemClick(arg0, mPosition);
        }
    }
}
