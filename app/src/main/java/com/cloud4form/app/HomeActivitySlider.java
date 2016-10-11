package com.cloud4form.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cloud4form.app.db.FormData;
import com.cloud4form.app.db.User;
import com.cloud4form.app.other.FormUploadService;
import com.cloud4form.app.other.JSONSync;
import com.cloud4form.app.pages.AccountViewFragment;
import com.cloud4form.app.pages.ChatProfileActivity;
import com.cloud4form.app.pages.FormViewFragment;
import com.cloud4form.app.pages.SettingsActivity;
import org.json.JSONObject;

public class HomeActivitySlider extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppController appController;
    private User user;
    private Fragment currentView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appController = AppController.getInstance(this);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View header=navigationView.getHeaderView(0);


        user=new User(appController.getAsJSON(AppController.PREE_USER_PROFILE));
        AppController.CURRENT_USER=user;

        ((TextView)header.findViewById(R.id.nav_top_name)).setText(user.getFirstName()+" "+user.getLastName());
        ((TextView)header.findViewById(R.id.nav_top_email)).setText(user.getEmail());

        if(user.getImage()!=null){
//            ((RoundedImageView)header.findViewById(R.id.NavviewImage)).setImageBitmap(user.profile);
        }

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(AccountViewFragment.newInstance("SELF","NONE"));
                setTitle("Profile");
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        switchFragment(FormViewFragment.newInstance());
        setTitle("Home");



        Intent myIntent = new Intent(this, FormUploadService.class);
        this.startService(myIntent);



//        appController.Filo.removeFileArray(FormData.class);
    }

    @Override
    protected void onStart() {
        super.onStart();


        if(appController.getPref(AppController.PREE_APP_WORK_MODE).equals(appController.getPref(AppController.PREE_APP_WORK_MODE_OFFLINE))){
            return;
        }
        registerBackground();

    }

    private void setPageTitle(String text){
        this.toolbar.setTitle(text);
    }

    private void registerBackground() {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String gToken= appController.checkGCMToken();
                if(gToken!=null && gToken.trim().length()>10){
                    try {
                        JSONObject req=new JSONObject();
                        req.put("GCM_TOKEN",gToken);
                        req.put("USER_ID", appController.getAsJSON(AppController.PREE_USER_PROFILE).getString("_id"));

                        JSONSync jsync = new JSONSync(null);
                        JSONObject resp=jsync.getJsonPost(appController.generateURL("api_url", "cgm_token"), req);
                    }catch (Exception ex){
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void msg) {
            }
        }.execute(null, null, null);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!(currentView instanceof FormViewFragment)) {
            switchFragment(FormViewFragment.newInstance());
            setTitle("Home");
        } else{
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    private void doLogout(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        appController.removePref(AppController.PREE_USER_PROFILE);
                        appController.removePref(AppController.PREE_APP_FORMS);
                        appController.removePref(AppController.PREE_SYNC_TOKEN);
                        appController.removePref(AppController.PREE_GCM_TOKEN);
                        AppController.CURRENT_USER=null;

                        Intent intent = new Intent(HomeActivitySlider.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to logout?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.nav_account:
                switchFragment(AccountViewFragment.newInstance("SELF","NONE"));
                setTitle("Profile");
                break;
            case R.id.nav_forms:
                switchFragment(FormViewFragment.newInstance());
                setTitle("Home");
                break;
            case R.id.nav_chat:
                Intent chatIntent=new Intent(HomeActivitySlider.this, ChatProfileActivity.class);
                startActivity(chatIntent);
//                switchFragment(ChatProfileFragment.newInstance());
//                setTitle("Message");
                break;
            case R.id.nav_settings:
                Intent settingIntent=new Intent(HomeActivitySlider.this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.nav_signout:
                doLogout();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchFragment(Fragment fragment){
        currentView=fragment;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.frame_content, fragment);
        ft.commit();
    }



}
