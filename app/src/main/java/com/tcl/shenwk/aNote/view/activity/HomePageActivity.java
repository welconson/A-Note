package com.tcl.shenwk.aNote.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.view.adapter.CustomAdapter;
import com.tcl.shenwk.aNote.view.navigationItem.NavigationItemHandler;

import java.util.List;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static String TAG = "HomePageActivity";
    private NavigationItemHandler navigationItemHandler;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        initializeView();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return navigationItemHandler.handleItemSelected(item);
    }

    public void initializeView(){
        setContentView(R.layout.home_page);
        if (getSupportActionBar() == null) {
            Log.i(TAG, "initializeView: return null");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.home_page_toolbar);
        final LayoutInflater inflater = getLayoutInflater();
        View view =  inflater.inflate(R.layout.tool_bar_button, null);
        toolbar.addView(view, -1, new Toolbar.LayoutParams(Gravity.END));
        view = inflater.inflate(R.layout.tool_bar_button, null);
        toolbar.addView(view, -1, new Toolbar.LayoutParams(Gravity.END));
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_note_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "add note", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(HomePageActivity.this, EditNoteActivity.class);
                intent.putExtra(Constants.ACTION_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_CREATE);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationItemHandler = new NavigationItemHandler(navigationView, drawer);
        mRecyclerView = findViewById(R.id.home_page_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(HomePageActivity.this));
        List<PreviewNoteEntry> previewNoteList = NoteHandler.getAllPreviewNoteList(HomePageActivity.this);
        Log.i(TAG, "initializeView: size " + previewNoteList.size());
        CustomAdapter customAdapter = new CustomAdapter(getLayoutInflater(),
               previewNoteList );
        mRecyclerView.setAdapter(customAdapter);
    }

    @Override
    protected void onPostResume() {
        Log.i(TAG, "onPostResume: running");
        Intent intent = getIntent();
        // Examine whether any note has been modified.
        // If modified, update RecyclerView data set and display.
        if(intent.getIntExtra(Constants.ACTION_TO_HOME_PAGE, Constants.HOME_PAGE_NORMAL_RESUME)
                == Constants.HOME_PAGE_UPDATE_RESUME) {
            CustomAdapter adapter = (CustomAdapter) mRecyclerView.getAdapter();
            String action = intent.getStringExtra(Constants.ACTION_EDIT_NOTE);
            PreviewNoteEntry previewNoteEntry = new PreviewNoteEntry();
            previewNoteEntry.noteEntry = (NoteEntry) intent.getSerializableExtra(Constants.ITEM_NOTE_ENTRY);
            previewNoteEntry.preResourceDataEntries = ANoteDBManager.getInstance(
                    HomePageActivity.this).queryAllResourceDataByNoteId(previewNoteEntry.noteEntry.getNoteId());
            if (StringUtil.equal(action, EditNoteActivity.EDIT_TYPE_MODIFY)) {
                adapter.refreshSingleItemByPosition(intent.getIntExtra(Constants.ITEM_POSITION, Constants.DEFAULT_ITEM_POSITION),
                        previewNoteEntry);
            } else {
                adapter.addItem(previewNoteEntry);
            }
            intent.putExtra(Constants.ACTION_TO_HOME_PAGE, Constants.HOME_PAGE_NORMAL_RESUME);
        }
        super.onPostResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public static class PreviewNoteEntry{
        public NoteEntry noteEntry;
        public List<ResourceDataEntry> preResourceDataEntries;
    }
}
