package com.tcl.shenwk.aNote.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.NoteTagEntry;
import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.fragment.AllNoteFragment;
import com.tcl.shenwk.aNote.view.fragment.TagManagerFragment;

import java.util.List;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static String TAG = "HomePageActivity";
    private RecyclerView recyclerView;
    private List<NoteEntry> noteEntries;
    private List<NoteTagEntry> noteTagEntries;
    private Menu mMenu;
    private int mCheckedMenuItemId;
    private DrawerLayout mDrawer;
    private FragmentManager mFragmentManager;

    private static final String ALL_NOTES_FRAGMENT_TAG = "all_notes";
    private static final String TAG_MANAGER_FRAGMENT_TAG = "tag_manager";
    private static final String UNARCHIVED_FRAGMENT_TAG = "unarchived";
    private static final String DISCARD_DRAWER_FRAGMENT_TAG = "discard_drawer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        setContentView(R.layout.home_page);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.home_page_toolbar);
        setSupportActionBar(toolbar);
//        final LayoutInflater inflater = getLayoutInflater();
//        View view =  inflater.inflate(R.layout.tool_bar_button, null);
//        toolbar.addView(view, -1, new Toolbar.LayoutParams(Gravity.END));
//        view = inflater.inflate(R.layout.tool_bar_button, null);
//        toolbar.addView(view, -1, new Toolbar.LayoutParams(Gravity.END));
        setNavigationDrawer(toolbar);

        mFragmentManager = getFragmentManager();

        Fragment fragment = new AllNoteFragment();
        mFragmentManager.beginTransaction()
                .add(R.id.content_main_frame, fragment, ALL_NOTES_FRAGMENT_TAG)
                .commit();

        noteEntries = ANoteDBManager.getInstance(HomePageActivity.this).queryAllNoteRecord();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.i(TAG, "onNavigationItemSelected: ");
        int id = item.getItemId();
        boolean isCheckedSameItem = false;
        Class fragmentClass = null;
        String tag = "";
        if(id == mCheckedMenuItemId) {
            isCheckedSameItem = true;
        }
        mCheckedMenuItemId = id;
        switch (id) {
            case R.id.all_note:
                if(noteEntries == null)
                    noteEntries = ANoteDBManager.getInstance(HomePageActivity.this).queryAllNoteRecord();
                fragmentClass = AllNoteFragment.class;
                tag = ALL_NOTES_FRAGMENT_TAG;
                break;
            case R.id.tag:
                if(noteTagEntries == null)
                    noteTagEntries = ANoteDBManager.getInstance(HomePageActivity.this).queryAllTag();
                fragmentClass = TagManagerFragment.class;
                tag = TAG_MANAGER_FRAGMENT_TAG;
                break;
            case R.id.archive:

                break;
            case R.id.discard_drawer:

                break;
            case R.id.nav_share:

                break;
            case R.id.nav_send:

                break;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        if(!isCheckedSameItem){
            if(fragmentClass != null) {
                try {
                    mFragmentManager.beginTransaction()
                            .replace(R.id.content_main_frame, ((Fragment) fragmentClass.newInstance()), tag)
                            .commit();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        // true to check the item is selected.
        return true;
    }

    @Override
    protected void onPostResume() {
        Log.i(TAG, "onPostResume: running");
        Intent intent = getIntent();
        // ensure which is the result from
        switch (intent.getIntExtra(Constants.RESULT_SOURCE_TO_HOME_PAGE, Constants.FROM_NO_WHERE)){
            case Constants.FROM_EDIT_ACTIVITY:
                AllNoteFragment allNoteFragment = (AllNoteFragment) mFragmentManager.findFragmentByTag("all_note");
                if(allNoteFragment != null){
                    OnEditActivityFinishedListener onEditActivityFinishedListener = allNoteFragment.getOnEditActivityFinishedListener();
                    if(onEditActivityFinishedListener != null) {
                        onEditActivityFinishedListener.onEditActivityFinished(intent);
                    }
                }
                break;
        }
        super.onPostResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public List<NoteEntry> getNoteEntries() {
        return noteEntries;
    }

    public List<NoteTagEntry> getNoteTagEntries() {
        return noteTagEntries;
    }

    public static class PreviewNoteEntry{
        public NoteEntry noteEntry;
        public List<ResourceDataEntry> preResourceDataEntries;
    }

    private void setNavigationDrawer(Toolbar toolbar){
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenu = navigationView.getMenu();
        mMenu.getItem(0).setChecked(true);
        mCheckedMenuItemId = mMenu.getItem(0).getItemId();
    }

    public interface OnEditActivityFinishedListener {
        void onEditActivityFinished(Intent intent);
    }
}
