package com.tcl.shenwk.aNote.view.navigationItem;

/**
 * Created by shenwk on 2018/1/24.
 */

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.tcl.shenwk.aNote.R;

/**
 * MainActivity navigation items handle class, once
 */
public class NavigationItemHandler {
    private NavigationView navigationView;
    private DrawerLayout drawer;

    public NavigationItemHandler(NavigationView navigationView, DrawerLayout drawer){
        this.drawer = drawer;
        this.navigationView = navigationView;
    }

    public boolean handleItemSelected(MenuItem item){
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
