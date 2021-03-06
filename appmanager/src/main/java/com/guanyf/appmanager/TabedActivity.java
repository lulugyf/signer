package com.guanyf.appmanager;

import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.guanyf.util.SimpleFileDialog;
import com.guanyf.util.Storage;

public class TabedActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Storage storage;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                pageSelected(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        setTitle(titles[0]);

        storage = new Storage(getSharedPreferences("config", 1));
    }

    private String[] titles = {"My App List", "Running Apps", "All Apps"};
    private void pageSelected(int position) {
        setTitle(titles[position]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabed, menu);
        return true;
    }

    private String favlist_file = "";
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_savelist) {
            SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(this, SimpleFileDialog.FileSave,
                    new SimpleFileDialog.SimpleFileDialogListener()
                    {
                        @Override
                        public void onChosenDir(String chosenDir)
                        {
                            // The code in this function will be executed when the dialog OK button is pushed
                            storage.exportList(chosenDir);
                            favlist_file = chosenDir;
                            Toast.makeText(TabedActivity.this, storage.status(), Toast.LENGTH_LONG).show();
                        }
                    });
            FileOpenDialog.Default_File_Name = favlist_file;
            FileOpenDialog.chooseFile_or_Dir();
        }else if(id == R.id.action_loadlist) {
            SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(this, SimpleFileDialog.FileOpen,
                    new SimpleFileDialog.SimpleFileDialogListener()
                    {
                        @Override
                        public void onChosenDir(String chosenDir)
                        {
                            // The code in this function will be executed when the dialog OK button is pushed
                            storage.importList(chosenDir);
                            favlist_file = chosenDir;
                            Toast.makeText(TabedActivity.this, storage.status(), Toast.LENGTH_LONG).show();
                        }
                    });
            FileOpenDialog.Default_File_Name = favlist_file;
            FileOpenDialog.chooseFile_or_Dir();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    return new Tab1();
                case 2:
                    return new Tab2();
                case 1:
                    return new Tab3();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "My App List";
                case 2:
                    return "All Apps";
                case 1:
                    return "Running Apps";
            }
            return null;
        }
    }
}
