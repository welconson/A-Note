package com.tcl.shenwk.aNote.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SearchRecentSuggestionsProvider;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import com.tcl.shenwk.aNote.data.ANoteContentProvider;
import com.tcl.shenwk.aNote.data.ContentProviderConstants;
import com.tcl.shenwk.aNote.data.DataProvider;
import com.tcl.shenwk.aNote.manager.LoginManager;
import com.tcl.shenwk.aNote.service.ANoteService;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.view.fragment.AllNoteFragment;
import com.tcl.shenwk.aNote.view.fragment.ArchivedFragment;
import com.tcl.shenwk.aNote.view.fragment.BaseFragment;
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
    private BaseFragment currentFragment;
    private OnKeyDownListener onKeyDownListener;
    private Toolbar toolbar;
    private long lastTime = 0;
    private BroadcastReceiver aNoteBroadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: sync finished");
            reload();
        }
    };

    private static final String ALL_NOTES_FRAGMENT_TAG = "All Notes";
    private static final String TAG_MANAGER_FRAGMENT_TAG = "Tag Manager";
    private static final String ARCHIVED_FRAGMENT_TAG = "Archived Notes";
    private static final String DISCARD_DRAWER_FRAGMENT_TAG = "Discard Drawer";

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

        BaseFragment fragment = new AllNoteFragment();
        currentFragment = fragment;
        mFragmentManager.beginTransaction()
                .add(R.id.content_main_frame, fragment, ALL_NOTES_FRAGMENT_TAG)
                .commit();

        startService(new Intent(getApplicationContext(), ANoteService.class));

        if (!FileUtil.isFileOrDirectoryExist(FileUtil.getTempDir(getApplicationContext()))) {
            FileUtil.createDir(FileUtil.getTempDir(getApplicationContext()));
        }

        bindService(new Intent(getApplicationContext(), ANoteService.class), conn, Service.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION_SYNC_MODIFIED);
        registerReceiver(aNoteBroadReceiver, intentFilter);
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
        boolean shouldCloseDrawer = true;
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
            case R.id.nav_sync:
                shouldCloseDrawer = false;
                if(aNoteService == null){
                    Toast.makeText(getApplicationContext(), R.string.toast_server_response_error, Toast.LENGTH_SHORT).show();
                }else {
                    aNoteService.sync();
                }
                break;
            case R.id.nav_exit:
                LoginManager.getInstance(getApplicationContext()).logOut(getApplicationContext());
                finish();
                break;
        }
        if(shouldCloseDrawer)
            mDrawer.closeDrawer(GravityCompat.START);
        if(!isCheckedSameItem){
            if(fragmentClass != null) {
                try {
                    BaseFragment fragment = (BaseFragment) fragmentClass.newInstance();
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


    private ANoteService.ANoteBinder aNoteService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            aNoteService = ((ANoteService.ANoteBinder) service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void reload(){
        ContentProviderClient contentProviderClient = getContentResolver().acquireUnstableContentProviderClient(ContentProviderConstants.NOTE_TABLE_URI);
        if(contentProviderClient != null){
            ANoteContentProvider aNoteContentProvider = ((ANoteContentProvider) contentProviderClient.getLocalContentProvider());
            if(aNoteContentProvider != null)
                aNoteContentProvider.resetDBHelper();
        }
        DataProvider.getInstance(getApplicationContext()).updateNoteEntity();
        DataProvider.getInstance(getApplicationContext()).updateAllTopTagEntity();
        if(currentFragment != null)
            currentFragment.reload();
    }
}
