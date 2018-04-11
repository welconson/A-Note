package com.tcl.shenwk.aNote.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.data.Test;
import com.tcl.shenwk.aNote.manager.LoginManager;
import com.tcl.shenwk.aNote.service.ANoteService;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.view.fragment.AllNoteFragment;
import com.tcl.shenwk.aNote.view.fragment.ArchivedFragment;
import com.tcl.shenwk.aNote.view.fragment.DiscardDrawerFragment;
import com.tcl.shenwk.aNote.view.fragment.TagManagerFragment;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static String TAG = "HomePageActivity";
    private static long EXIT_TIME_THRESHOLD = 4000;
    private Menu mMenu;
    private int mCheckedMenuItemId;
    private DrawerLayout mDrawer;
    private FragmentManager mFragmentManager;
    private Fragment currentFragment;
    private OnKeyDownListener onKeyDownListener;
    private Toolbar toolbar;
    private long lastTime = 0;

    private static final String ALL_NOTES_FRAGMENT_TAG = "all notes";
    private static final String TAG_MANAGER_FRAGMENT_TAG = "tag manager";
    private static final String ARCHIVED_FRAGMENT_TAG = "archived notes";
    private static final String DISCARD_DRAWER_FRAGMENT_TAG = "discard drawer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        setContentView(R.layout.home_page);

        // Set toolbar
        toolbar = (Toolbar) findViewById(R.id.home_page_toolbar);
        toolbar.setTitle(ALL_NOTES_FRAGMENT_TAG);
        setSupportActionBar(toolbar);
//        final LayoutInflater inflater = getLayoutInflater();
//        View view =  inflater.inflate(R.layout.tool_bar_button, null);
//        toolbar.addView(view, -1, new Toolbar.LayoutParams(Gravity.END));
//        view = inflater.inflate(R.layout.tool_bar_button, null);
//        toolbar.addView(view, -1, new Toolbar.LayoutParams(Gravity.END));
        setNavigationDrawer(toolbar);

        mFragmentManager = getFragmentManager();

        Fragment fragment = new AllNoteFragment();
        currentFragment = fragment;
        mFragmentManager.beginTransaction()
                .add(R.id.content_main_frame, fragment, ALL_NOTES_FRAGMENT_TAG)
                .commit();

        startService(new Intent(getApplicationContext(), ANoteService.class));

        if (!FileUtil.isFileOrDirectoryExist(FileUtil.getTempDir(getApplicationContext()))) {
            FileUtil.createDir(FileUtil.getTempDir(getApplicationContext()));
        }

        Test test = new Test();
        test.query(getContentResolver());
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
                fragmentClass = AllNoteFragment.class;
                tag = ALL_NOTES_FRAGMENT_TAG;
                break;
            case R.id.tag:
                fragmentClass = TagManagerFragment.class;
                tag = TAG_MANAGER_FRAGMENT_TAG;
                break;
            case R.id.archive:
                fragmentClass = ArchivedFragment.class;
                tag = ARCHIVED_FRAGMENT_TAG;
                break;
            case R.id.discard_drawer:
                fragmentClass = DiscardDrawerFragment.class;
                tag = DISCARD_DRAWER_FRAGMENT_TAG;
                break;
            case R.id.nav_share:

                break;
            case R.id.exit:
                LoginManager.getInstance(getApplicationContext()).logOut(getApplicationContext());
                finish();
                break;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        if(!isCheckedSameItem){
            if(fragmentClass != null) {
                try {
                    Fragment fragment = (Fragment) fragmentClass.newInstance();
                    currentFragment = fragment;
                    mFragmentManager.beginTransaction()
                            .replace(R.id.content_main_frame, fragment, tag)
                            .commit();
                    if(fragment instanceof TagManagerFragment)
                        onKeyDownListener = (TagManagerFragment)fragment;
                    toolbar.setTitle(tag);
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
        super.onPostResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(onKeyDownListener != null)
            if(onKeyDownListener.onKeyDown(keyCode, event))
                return true;
        long nowTime = DateUtil.getInstance().getTime();
        if(nowTime - lastTime > EXIT_TIME_THRESHOLD){
            lastTime = nowTime;
            Toast.makeText(HomePageActivity.this, R.string.toast_exit_hint, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    public interface OnKeyDownListener{
        boolean onKeyDown(int keyCode, KeyEvent keyEvent);
    }
}
